package com.leancam.employeemanager.config;

import com.leancam.employeemanager.service.EmployeeTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider employeeToolProvider(EmployeeTools employeeTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(employeeTools)
                .build();
    }
}