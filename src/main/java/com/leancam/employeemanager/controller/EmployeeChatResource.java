package com.leancam.employeemanager.controller;

import com.leancam.employeemanager.dto.ChatRequest;
import com.leancam.employeemanager.dto.ChatResponse;
import com.leancam.employeemanager.service.EmployeeAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class EmployeeChatResource {

    private final EmployeeAIService employeeAIService;

    @Autowired
    public EmployeeChatResource(EmployeeAIService employeeAIService) {
        this.employeeAIService = employeeAIService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = employeeAIService.chat(request.getMessage());
        return new ResponseEntity<>(new ChatResponse(reply), HttpStatus.OK);
    }
}