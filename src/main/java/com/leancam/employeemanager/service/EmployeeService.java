package com.leancam.employeemanager.service;

import com.leancam.employeemanager.event.EmployeeEvent;
import com.leancam.employeemanager.exception.UserNotFoundException;
import com.leancam.employeemanager.kafka.EmployeeEventProducer;
import com.leancam.employeemanager.model.Employee;
import com.leancam.employeemanager.repo.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class EmployeeService {

    private static final int BATCH_SIZE = 100;

    private final EmployeeRepo employeeRepo;
    private final EmployeeEventProducer employeeEventProducer;
    private final Executor bulkInsertExecutor;

    @Autowired
    public EmployeeService(
            EmployeeRepo employeeRepo,
            EmployeeEventProducer employeeEventProducer,
            @Qualifier("bulkInsertExecutor") Executor bulkInsertExecutor) {
        this.employeeRepo = employeeRepo;
        this.employeeEventProducer = employeeEventProducer;
        this.bulkInsertExecutor = bulkInsertExecutor;
    }

    public Employee addEmployee(Employee employee) {
        employee.setEmployeeCode(UUID.randomUUID().toString());
        Employee savedEmployee = employeeRepo.save(employee);

        employeeEventProducer.publish(
                "employee.events",
                String.valueOf(savedEmployee.getId()),
                EmployeeEvent.builder()
                        .eventType("CREATED")
                        .employeeId(savedEmployee.getId())
                        .eventTime(Instant.now())
                        .build()
        );
        return savedEmployee;
    }

    public List<Employee> addEmployees(List<Employee> employees) {

        // Step 1: Assign UUIDs in parallel — UUID.randomUUID() is thread-safe
        employees.parallelStream()
                .forEach(e -> e.setEmployeeCode(UUID.randomUUID().toString()));

        // Step 2: Split into fixed-size batches
        List<List<Employee>> batches = partition(employees, BATCH_SIZE);

        // Step 3: Submit each batch as an async task on bulkInsertExecutor
        List<CompletableFuture<List<Employee>>> futures = batches.stream()
                .map(batch -> CompletableFuture
                        .supplyAsync(() -> saveBatch(batch), bulkInsertExecutor)
                        .exceptionally(ex -> {
                            System.err.println("Batch insert failed: " + ex.getMessage());
                            return List.of();
                        })
                )
                .toList();

        // Step 4: Wait for all batches to complete and flatten results
        List<Employee> savedEmployees = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();

        // Step 5: Publish Kafka CREATED events for every saved employee
        savedEmployees.forEach(saved ->
                employeeEventProducer.publish(
                        "employee.events",
                        String.valueOf(saved.getId()),
                        EmployeeEvent.builder()
                                .eventType("CREATED")
                                .employeeId(saved.getId())
                                .eventTime(Instant.now())
                                .build()
                )
        );

        return savedEmployees;
    }

    // Each batch runs in its own transaction on its own thread
    @Transactional
    public List<Employee> saveBatch(List<Employee> batch) {
        return employeeRepo.saveAll(batch);
    }

    // Splits a list into sublists of the given size
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    public List<Employee> findAllEmployees(){
        return employeeRepo.findAll();
    }

    public Employee updateEmployee(Employee employee){
        return employeeRepo.save(employee);
    }

    public Employee findEmployeeById(Long id){
        return employeeRepo.findEmployeeById(id)
                .orElseThrow(() -> new UserNotFoundException(" User by id " + id + " was not found"));
    }

    public Employee findEmployeeByEmail(String email){
        return employeeRepo.findEmployeeByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(" User by email " + email + " was not found"));
    }

    public void deleteEmployee(Long id){
        employeeRepo.deleteEmployeeById(id);
    }

}
