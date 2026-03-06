package com.leancam.employeemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leancam.employeemanager.exception.UserNotFoundException;
import com.leancam.employeemanager.model.Employee;
import com.leancam.employeemanager.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeResource.class)
class EmployeeResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

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
        employee.setEmployeeCode("EMP-001");
    }

    // GET /employee/all
    @Test
    void getAllEmployees_ShouldReturnListAndStatus200() throws Exception {
        when(employeeService.findAllEmployees()).thenReturn(List.of(employee));

        mockMvc.perform(get("/employee/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployees() throws Exception {
        when(employeeService.findAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/employee/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // GET /employee/find/{id}
    @Test
    void getEmployeeById_ShouldReturnEmployeeAndStatus200() throws Exception {
        when(employeeService.findEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(get("/employee/find/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getEmployeeById_ShouldReturnStatus404_WhenNotFound() throws Exception {
        when(employeeService.findEmployeeById(99L)).thenThrow(new UserNotFoundException("User by id 99 was not found"));

        mockMvc.perform(get("/employee/find/99"))
                .andExpect(status().isNotFound());
    }

    // GET /employee/findEmail/{email}
    @Test
    void getEmployeeByEmail_ShouldReturnEmployeeAndStatus200() throws Exception {
        when(employeeService.findEmployeeByEmail("john@example.com")).thenReturn(employee);

        mockMvc.perform(get("/employee/findEmail/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getEmployeeByEmail_ShouldReturnStatus404_WhenNotFound() throws Exception {
        when(employeeService.findEmployeeByEmail("unknown@example.com"))
                .thenThrow(new UserNotFoundException("User by email unknown@example.com was not found"));

        mockMvc.perform(get("/employee/findEmail/unknown@example.com"))
                .andExpect(status().isNotFound());
    }

    // POST /employee/add
    @Test
    void addEmployee_ShouldReturnSavedEmployeeAndStatus201() throws Exception {
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/employee/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-001"));
    }

    // POST /employee/add/bulk
    @Test
    void addEmployees_ShouldReturnSavedListAndStatus201() throws Exception {
        when(employeeService.addEmployees(any())).thenReturn(List.of(employee));

        mockMvc.perform(post("/employee/add/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(employee))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    // PUT /employee/update
    @Test
    void updateEmployee_ShouldReturnUpdatedEmployeeAndStatus200() throws Exception {
        employee.setName("Jane Doe");
        when(employeeService.updateEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(put("/employee/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    // DELETE /employee/delete/{id}
    @Test
    void deleteEmployee_ShouldReturnStatus200() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/employee/delete/1"))
                .andExpect(status().isOk());

        verify(employeeService).deleteEmployee(1L);
    }
}
