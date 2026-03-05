package com.leancam.employeemanager.service;

import com.leancam.employeemanager.event.EmployeeEvent;
import com.leancam.employeemanager.exception.UserNotFoundException;
import com.leancam.employeemanager.kafka.EmployeeEventProducer;
import com.leancam.employeemanager.model.Employee;
import com.leancam.employeemanager.repo.EmployeeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepo employeeRepo;

    @Mock
    private EmployeeEventProducer employeeEventProducer;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setEmail("john@example.com");
        employee.setJobTitle("Engineer");
        employee.setPhone("1234567890");
        employee.setSalary(50000L);
    }

    // addEmployee()
    @Test
    void addEmployee_ShouldSetEmployeeCode() {
        when(employeeRepo.save(any(Employee.class))).thenReturn(employee);

        employeeService.addEmployee(employee);

        assertThat(employee.getEmployeeCode()).isNotNull().isNotEmpty();
    }

    @Test
    void addEmployee_ShouldReturnSavedEmployee() {
        when(employeeRepo.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.addEmployee(employee);

        assertThat(result).isEqualTo(employee);
    }

    @Test
    void addEmployee_ShouldPublishCreatedKafkaEvent() {
        when(employeeRepo.save(any(Employee.class))).thenReturn(employee);

        employeeService.addEmployee(employee);

        ArgumentCaptor<EmployeeEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeEvent.class);
        verify(employeeEventProducer).publish(eq("employee.events"), eq("1"), eventCaptor.capture());

        EmployeeEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo("CREATED");
        assertThat(capturedEvent.getEmployeeId()).isEqualTo(1L);
        assertThat(capturedEvent.getEventTime()).isNotNull();
    }

    // addEmployees()
    @Test
    void addEmployees_ShouldSetEmployeeCodeOnEach() {
        Employee e1 = new Employee();
        Employee e2 = new Employee();
        List<Employee> employees = List.of(e1, e2);
        when(employeeRepo.saveAll(employees)).thenReturn(employees);

        employeeService.addEmployees(employees);

        assertThat(e1.getEmployeeCode()).isNotNull();
        assertThat(e2.getEmployeeCode()).isNotNull();
        assertThat(e1.getEmployeeCode()).isNotEqualTo(e2.getEmployeeCode());
    }

    @Test
    void addEmployees_ShouldReturnSavedEmployees() {
        List<Employee> employees = List.of(employee);
        when(employeeRepo.saveAll(employees)).thenReturn(employees);

        List<Employee> result = employeeService.addEmployees(employees);

        assertThat(result).hasSize(1).containsExactly(employee);
    }

    // findAllEmployees()
    @Test
    void findAllEmployees_ShouldReturnList() {
        when(employeeRepo.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.findAllEmployees();

        assertThat(result).hasSize(1).containsExactly(employee);
    }

    // findEmployeeById()
    @Test
    void findEmployeeById_ShouldReturnEmployee_WhenFound() {
        when(employeeRepo.findEmployeeById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.findEmployeeById(1L);

        assertThat(result).isEqualTo(employee);
    }

    @Test
    void findEmployeeById_ShouldThrowUserNotFoundException_WhenNotFound() {
        when(employeeRepo.findEmployeeById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findEmployeeById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    // findEmployeeByEmail()
    @Test
    void findEmployeeByEmail_ShouldReturnEmployee_WhenFound() {
        when(employeeRepo.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));

        Employee result = employeeService.findEmployeeByEmail("john@example.com");

        assertThat(result).isEqualTo(employee);
    }

    @Test
    void findEmployeeByEmail_ShouldThrowUserNotFoundException_WhenNotFound() {
        when(employeeRepo.findEmployeeByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findEmployeeByEmail("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("unknown@example.com");
    }

    // updateEmployee()
    @Test
    void updateEmployee_ShouldReturnUpdatedEmployee() {
        employee.setName("Jane Doe");
        when(employeeRepo.save(employee)).thenReturn(employee);

        Employee result = employeeService.updateEmployee(employee);

        assertThat(result.getName()).isEqualTo("Jane Doe");
        verify(employeeRepo).save(employee);
    }

    // deleteEmployee()
    @Test
    void deleteEmployee_ShouldCallDeleteById() {
        doNothing().when(employeeRepo).deleteEmployeeById(1L);

        employeeService.deleteEmployee(1L);

        verify(employeeRepo).deleteEmployeeById(1L);
    }
}
