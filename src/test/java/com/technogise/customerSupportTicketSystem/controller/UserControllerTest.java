package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.controller.UserController;
import com.technogise.customerSupportTicketSystem.dto.RegisterUserRequest;
import com.technogise.customerSupportTicketSystem.dto.RegisterUserResponse;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import com.technogise.customerSupportTicketSystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterUserResponse mockResponse;
    private RegisterUserRequest validRequest;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        validRequest = new RegisterUserRequest(
                "Jatin Kumar",
                "jatin@gmail.com",
                "Password@123"
        );

        mockResponse = new RegisterUserResponse(
                "Jatin Kumar",
                "jatin@gmail.com",
                userId
        );
    }

    @Test
    void shouldReturn201_WhenUserCreatedSuccessfully() throws Exception {
        when(userService.registerUser(any(RegisterUserRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.name").value("Jatin Kumar"))
                .andExpect(jsonPath("$.data.email").value("jatin@gmail.com"))
                .andExpect(jsonPath("$.data.id").exists());
    }
}