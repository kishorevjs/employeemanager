package com.leancam.employeemanager.service;

import com.leancam.employeemanager.model.Employee;
import com.leancam.employeemanager.repo.EmployeeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeToolsTest {

    @Mock
    private EmployeeRepo employeeRepo;

    @InjectMocks
    private EmployeeTools employeeTools;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setEmail("john@example.com");
        employee.setJobTitle("Engineer");
    }

    // getAllEmployees()
    @Test
    void getAllEmployees_ShouldReturnAllEmployees() {
        when(employeeRepo.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeTools.getAllEmployees();

        assertThat(result).hasSize(1).containsExactly(employee);
        verify(employeeRepo).findAll();
    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployees() {
        when(employeeRepo.findAll()).thenReturn(List.of());

        List<Employee> result = employeeTools.getAllEmployees();

        assertThat(result).isEmpty();
    }

    // getEmployeeById()
    @Test
    void getEmployeeById_ShouldReturnEmployee_WhenFound() {
        when(employeeRepo.findEmployeeById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeTools.getEmployeeById(1L);

        assertThat(result).isEqualTo(employee);
    }

    @Test
    void getEmployeeById_ShouldReturnNull_WhenNotFound() {
        when(employeeRepo.findEmployeeById(99L)).thenReturn(Optional.empty());

        Employee result = employeeTools.getEmployeeById(99L);

        assertThat(result).isNull();
    }

    // getEmployeeByEmail()
    @Test
    void getEmployeeByEmail_ShouldReturnEmployee_WhenFound() {
        when(employeeRepo.findEmployeeByEmail("john@example.com")).thenReturn(Optional.of(employee));

        Employee result = employeeTools.getEmployeeByEmail("john@example.com");

        assertThat(result).isEqualTo(employee);
    }

    @Test
    void getEmployeeByEmail_ShouldReturnNull_WhenNotFound() {
        when(employeeRepo.findEmployeeByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Employee result = employeeTools.getEmployeeByEmail("unknown@example.com");

        assertThat(result).isNull();
    }
}
