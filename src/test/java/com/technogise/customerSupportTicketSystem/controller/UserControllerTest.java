package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.config.SecurityConfig;
import com.technogise.customerSupportTicketSystem.dto.UserResponse;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import com.technogise.customerSupportTicketSystem.service.JwtService;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import com.technogise.customerSupportTicketSystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private TicketService ticketService;

    private User mockAgent;

    @BeforeEach
    void setup() {
        mockAgent = new User();
        mockAgent.setId(UUID.randomUUID());
        mockAgent.setRole(UserRole.SUPPORT_AGENT);
        mockAgent.setEmail("virat@gmail.com");
    }

    private Authentication authFor(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void shouldReturn200WithUserList_WhenAuthenticatedUserFetches_AllSupportAgents() throws Exception {
        // Given
        List<UserResponse> mockUsers = List.of(
                new UserResponse(UUID.randomUUID(), "Raj", "raj@gmail.com", "SUPPORT_AGENT", null),
                new UserResponse(UUID.randomUUID(), "Rakshit", "rakshit@gmail.com", "SUPPORT_AGENT", null)
        );

        when(userService.getAllUsersByRole(mockAgent.getId(), UserRole.SUPPORT_AGENT))
                .thenReturn(mockUsers);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .with(authentication(authFor(mockAgent)))
                        .param("role", "SUPPORT_AGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users fetched successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Raj"))
                .andExpect(jsonPath("$.data[1].name").value("Rakshit"))
                .andExpect(jsonPath("$.data[0].email").value("raj@gmail.com"))
                .andExpect(jsonPath("$.data[1].email").value("rakshit@gmail.com"))
                .andExpect(jsonPath("$.data[0].role").value("SUPPORT_AGENT"));
    }

    @Test
    void shouldReturn200WithEmptyList_WhenNoUsersFoundForRole() throws Exception {
        // Given
        when(userService.getAllUsersByRole(mockAgent.getId(), UserRole.SUPPORT_AGENT))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("role", "SUPPORT_AGENT")
                        .with(authentication(authFor(mockAgent))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldReturn400_WhenRoleParamIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                        .with(authentication(authFor(mockAgent))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_WhenRoleParamIsInvalid() throws Exception {
        // Given - invalid role string

        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("role", "INVALID_ROLE")
                        .with(authentication(authFor(mockAgent))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAuthenticatedUser_shouldReturn200_withUserResponse() throws Exception {
        // Given
        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "Raj",
                "raj@gmail.com",
                "SUPPORT_AGENT",
                List.of(
                        "VIEW_ASSIGNED_TICKETS",
                        "UPDATE_TICKET_STATUS",
                        "UPDATE_TICKET_PRIORITY",
                        "REASSIGN_TICKET"
                )
        );
        when(userService.getAuthenticatedUser(mockAgent.getId())).thenReturn(response);

        // When and Then
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(authFor(mockAgent))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Raj"))
                .andExpect(jsonPath("$.email").value("raj@gmail.com"))
                .andExpect(jsonPath("$.role").value("SUPPORT_AGENT"))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions", hasItem("REASSIGN_TICKET")));
    }

    @Test
    void getAuthenticatedUser_shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.getAuthenticatedUser(mockAgent.getId()))
                .thenThrow(new ResourceNotFoundException("USER_NOT_FOUND", "User not found with id: " + mockAgent.getId()));

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(authFor(mockAgent))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with id: " + mockAgent.getId()));
    }
}
