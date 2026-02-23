package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.config.SecurityConfig;
import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentRequest;
import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentResponse;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import com.technogise.customerSupportTicketSystem.service.JwtService;
import com.technogise.customerSupportTicketSystem.service.TicketAssignmentService;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketAssignmentController.class)
@Import(SecurityConfig.class)
class TicketAssignmentControllerTest {

    @MockitoBean
    private TicketAssignmentService ticketAssignmentService;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("Admin");
        mockUser.setRole(UserRole.SUPPORT_AGENT);
    }

    private Authentication authFor(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void shouldReturnStatus201CreatedAndTicketAssignmentResponse_WhenTicketAssignedSuccessfully() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UUID assignedToUserId = UUID.randomUUID();
        UUID assignedByUserId = UUID.randomUUID();

        TicketAssignmentRequest ticketAssignmentRequest = new TicketAssignmentRequest();
        ticketAssignmentRequest.setAssignedToUserId(assignedToUserId);
        ticketAssignmentRequest.setAssignedByUserId(assignedByUserId);

        TicketAssignmentResponse ticketAssignmentResponse = new TicketAssignmentResponse(
                UUID.randomUUID(),
                ticketId,
                assignedToUserId,
                assignedByUserId
        );

        Mockito.when(ticketAssignmentService.assignTicket(
                Mockito.eq(ticketId),
                Mockito.eq(assignedByUserId),
                Mockito.eq(assignedToUserId)
        )).thenReturn(ticketAssignmentResponse);

        mockMvc.perform(post("/api/tickets/{id}/assign", ticketId)
                        .with(authentication(authFor(mockUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketAssignmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.data.assignedToUserId").value(assignedToUserId.toString()))
                .andExpect(jsonPath("$.data.assignedByUserId").value(assignedByUserId.toString()))
                .andExpect(jsonPath("$.message").value("Ticket assigned successfully"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn400_WhenAssignedByUserIdFormatIsInvalid() throws Exception {
        UUID ticketId = UUID.randomUUID();

        String invalidJson = """
                {
                  "assignedToUserId": "501a4912-4351-428b-a4ed-971e28ee1086",
                  "assignedByUserId": "invalid-uuid"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tickets/{id}/assign", ticketId)
                        .with(authentication(authFor(mockUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_WhenAssignedToUserIdFormatIsInvalid() throws Exception {
        UUID ticketId = UUID.randomUUID();

        String invalidJson = """
                {
                  "assignedToUserId": "invalid-uuid",
                  "assignedByUserId": "501a4912-4351-428b-a4ed-971e28ee1086"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tickets/{id}/assign", ticketId)
                        .with(authentication(authFor(mockUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

}