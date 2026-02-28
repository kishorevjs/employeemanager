package com.leancam.employeemanager.service;

import com.leancam.employeemanager.model.Employee;
import com.leancam.employeemanager.repo.EmployeeRepo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeTools {

    private final EmployeeRepo employeeRepo;

    @Autowired
    public EmployeeTools(EmployeeRepo employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    @Tool(description = "Get all employees from the employee roster")
    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    @Tool(description = "Find a specific employee by their numeric ID")
    public Employee getEmployeeById(Long id) {
        return employeeRepo.findEmployeeById(id).orElse(null);
    }

    @Tool(description = "Find a specific employee by their email address")
    public Employee getEmployeeByEmail(String email) {
        return employeeRepo.findEmployeeByEmail(email).orElse(null);
    }
}
