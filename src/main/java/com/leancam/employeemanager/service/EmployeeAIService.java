package com.leancam.employeemanager.service;

import com.leancam.employeemanager.model.Employee;
import com.leancam.employeemanager.repo.EmployeeRepo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeAIService {

    private final ChatClient chatClient;
    private final EmployeeRepo employeeRepo;

    @Autowired
    public EmployeeAIService(ChatClient.Builder chatClientBuilder, EmployeeRepo employeeRepo) {
        this.chatClient = chatClientBuilder.build();
        this.employeeRepo = employeeRepo;
    }

    public String chat(String userMessage) {
        List<Employee> employees = employeeRepo.findAll();
        String employeeData = formatEmployees(employees);

        String systemPrompt = """
                You are an AI HR Assistant for a company. You have access to the current employee roster below.
                Answer HR-related questions based on this data. Be concise, professional, and helpful.
                You can answer questions about headcount, salaries, job titles, team composition, promotions,
                and any other HR-related topics. If a question cannot be answered from the data provided, say so clearly.

                Current Employee Roster:
                """ + employeeData;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }

    private String formatEmployees(List<Employee> employees) {
        if (employees.isEmpty()) {
            return "No employees found in the system.";
        }
        return employees.stream()
                .map(e -> String.format("- ID: %d | Name: %s | Title: %s | Email: %s | Phone: %s | Salary: $%d",
                        e.getId(), e.getName(), e.getJobTitle(), e.getEmail(), e.getPhone(), e.getSalary()))
                .collect(Collectors.joining("\n"));
    }
}