package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.CreateUserRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateUserResponse;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import tools.jackson.databind.ObjectMapper;
import  org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserResponse createUserResponse;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        createUserResponse = new CreateUserResponse();
        createUserResponse.setEmail("abc@email.com");
        createUserResponse.setName("Abc");
        createUserResponse.setRole(UserRole.CUSTOMER);

        createUserRequest = new CreateUserRequest();
        createUserRequest.setName("Abc");
        createUserRequest.setEmail("abc@email.com");
        createUserRequest.setRole(UserRole.CUSTOMER);
    }

    @Test
    void shouldReturn201_WhenUserCreatedSuccessfully() throws Exception {

        when(userService.createUser(
                eq(createUserRequest.getName()),
                eq(createUserRequest.getRole()),
                eq(createUserRequest.getEmail())
        )).thenReturn(createUserResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.name").value("Abc"))
                .andExpect(jsonPath("$.data.email").value("abc@email.com"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

}