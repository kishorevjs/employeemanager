package com.leancam.employeemanager.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeAIService {

    private final ChatClient chatClient;

    @Autowired
    public EmployeeAIService(ChatClient.Builder chatClientBuilder, EmployeeTools employeeTools) {
        this.chatClient = chatClientBuilder
                .defaultTools(employeeTools)
                .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .system("""
                        You are an AI HR Assistant. Use the available tools to look up employee data
                        when needed. Be concise, professional, and helpful. If a question cannot be
                        answered from the available data, say so clearly.
                        """)
                .user(userMessage)
                .call()
                .content();
    }
}