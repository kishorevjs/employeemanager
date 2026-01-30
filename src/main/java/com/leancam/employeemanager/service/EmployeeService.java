package com.leancam.employeemanager.service;

import com.leancam.employeemanager.event.EmployeeEvent;
import com.leancam.employeemanager.exception.UserNotFoundException;
import com.leancam.employeemanager.kafka.EmployeeEventProducer;
import com.leancam.employeemanager.model.Employee;
import com.leancam.employeemanager.repo.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepo employeeRepo;
    private final EmployeeEventProducer employeeEventProducer;

    @Autowired
    public EmployeeService(EmployeeRepo employeeRepo, EmployeeEventProducer employeeEventProducer) {
        this.employeeRepo = employeeRepo;
        this.employeeEventProducer = employeeEventProducer;
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

    /*public Employee addEmployee(Employee employee){
        employee.setEmployeeCode(UUID.randomUUID().toString());
        return employeeRepo.save(employee);
    }*/

    public List<Employee> addEmployees(List<Employee> employees){
        employees.forEach(e -> e.setEmployeeCode(UUID.randomUUID().toString()));
        return employeeRepo.saveAll(employees);
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
