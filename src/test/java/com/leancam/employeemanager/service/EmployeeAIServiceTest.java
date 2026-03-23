package com.leancam.employeemanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeAIServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private EmployeeTools employeeTools;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private EmployeeAIService employeeAIService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultTools(any(EmployeeTools.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        employeeAIService = new EmployeeAIService(chatClientBuilder, employeeTools);
    }

    // chat()
    @Test
    void chat_ShouldReturnResponseFromChatClient() {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Here are the employees.");

        String result = employeeAIService.chat("List all employees");

        assertThat(result).isEqualTo("Here are the employees.");
    }

    @Test
    void chat_ShouldPassUserMessageToChatClient() {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user("Who is the manager?")).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("I don't have that information.");

        employeeAIService.chat("Who is the manager?");

        verify(chatClientRequestSpec).user("Who is the manager?");
    }

    @Test
    void chat_ShouldReturnNull_WhenChatClientReturnsNull() {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(null);

        String result = employeeAIService.chat("Hello");

        assertThat(result).isNull();
    }

    @Test
    void chat_ShouldApplySystemPrompt() {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("response");

        employeeAIService.chat("test");

        verify(chatClientRequestSpec).system(anyString());
    }
}