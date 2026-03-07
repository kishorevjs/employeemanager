package com.leancam.employeemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leancam.employeemanager.dto.ChatRequest;
import com.leancam.employeemanager.service.EmployeeAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeChatResource.class)
class EmployeeChatResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeAIService employeeAIService;

    @Autowired
    private ObjectMapper objectMapper;

    // POST /employee/chat
    @Test
    void chat_ShouldReturnReplyAndStatus200() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("Who is the CEO?");

        when(employeeAIService.chat("Who is the CEO?")).thenReturn("I don't have that information.");

        mockMvc.perform(post("/employee/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("I don't have that information."));
    }

    @Test
    void chat_ShouldDelegateMessageToAIService() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("List all employees");

        when(employeeAIService.chat("List all employees")).thenReturn("Here are the employees...");

        mockMvc.perform(post("/employee/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(employeeAIService).chat("List all employees");
    }

    @Test
    void chat_ShouldReturnEmptyReply_WhenAIReturnsEmpty() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello");

        when(employeeAIService.chat("Hello")).thenReturn("");

        mockMvc.perform(post("/employee/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value(""));
    }

    @Test
    void chat_ShouldPropagateException_WhenAIServiceThrowsException() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("crash");

        when(employeeAIService.chat("crash")).thenThrow(new RuntimeException("AI service unavailable"));


        assertThrows(Exception.class, () ->
                mockMvc.perform(post("/employee/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
        );
    }
}