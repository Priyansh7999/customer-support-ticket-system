package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.technogise.customerSupportTicketSystem.dto.AgentTicketResponse;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

        @MockitoBean
        private TicketService ticketService;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private CreateTicketRequest request;
        private User customer;
        private User supportAgent;
        private CreateTicketResponse mockTicket;

        @BeforeEach
        void setup() {
                request = new CreateTicketRequest();
                request.setTitle("Sample Ticket");
                request.setDescription("This is a sample ticket description.");

                customer = new User();
                customer.setId(UUID.randomUUID());
                customer.setName("Raj");
                customer.setRole(UserRole.CUSTOMER);

                supportAgent = new User();
                supportAgent.setId(UUID.randomUUID());
                supportAgent.setName("Support Agent");
                supportAgent.setRole(UserRole.SUPPORT_AGENT);

                mockTicket = new CreateTicketResponse();
                mockTicket.setId(UUID.randomUUID());
                mockTicket.setTitle(request.getTitle());
                mockTicket.setDescription(request.getDescription());
                mockTicket.setStatus(TicketStatus.OPEN);
                mockTicket.setAssignedToName(supportAgent.getName());
                mockTicket.setCreatedAt(LocalDateTime.now());
        }

        @Test
        void shouldReturn201AndTicket_WhenCreateTicketSuccessfully() throws Exception {
                // Given
                when(ticketService.createTicket(request.getTitle(), request.getDescription(), customer.getId()))
                                .thenReturn(mockTicket);

                // When and Then
                mockMvc.perform(post("/api/tickets")
                                .header(Constants.USER_ID, customer.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Ticket created successfully"))
                                .andExpect(jsonPath("$.data.title").value("Sample Ticket"))
                                .andExpect(jsonPath("$.data.description")
                                                .value("This is a sample ticket description."));
        }

        @Test
        void shouldReturn400_WhenTitleIsNull() throws Exception {
                // Given
                request.setTitle(null);

                // When and Then
                mockMvc.perform(post("/api/tickets")
                                .header(Constants.USER_ID, customer.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                                .andExpect(jsonPath("$.message").value("Title is required"));
        }

        @Test
        void shouldReturn400_WhenDescriptionIsNull() throws Exception {
                // Given
                request.setDescription(null);

                // When and Then
                mockMvc.perform(post("/api/tickets")
                                .header(Constants.USER_ID, customer.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                                .andExpect(jsonPath("$.message").value("Description is required"));
        }

        @Test
        void shouldReturn400_WhenUserIdHeaderIsMissing() throws Exception {
                // Given
                when(ticketService.createTicket(request.getTitle(), request.getDescription(), customer.getId()))
                                .thenReturn(mockTicket);

                // When and Then
                mockMvc.perform(post("/api/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("MISSING_USER_ID"))
                                .andExpect(jsonPath("$.message").value("Missing required request header: User-Id"));
        }

        @Test
        void shouldReturn400_WhenTitleExceedsMaxLength() throws Exception {
                // Given
                request.setTitle("A".repeat(101)); // Title with 101 characters

                // When and Then
                mockMvc.perform(post("/api/tickets")
                                .header(Constants.USER_ID, customer.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                                .andExpect(jsonPath("$.message").value("Title must not exceed 100 characters"));
        }

        @Test
        void shouldReturn400_WhenDescriptionExceedsMaxLength() throws Exception {
                // Given
                request.setDescription("A".repeat(1001)); // Title with 101 characters

                // When and Then
                mockMvc.perform(post("/api/tickets")
                                .header(Constants.USER_ID, customer.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                                .andExpect(jsonPath("$.message").value("Description must not exceed 1000 characters"));
        }
    @Test
    void shouldReturn201_WhenCommentAddedSuccessfully() throws Exception {
        // Given
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("comment");
        CreateCommentResponse response = new CreateCommentResponse();
        response.setBody("comment");
        when(ticketService.addComment(eq(ticketId), any(CreateCommentRequest.class), eq(userId))).thenReturn(response);
        // When & Then
        mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                        .header(Constants.USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.body").value("comment"));
    }

        @Test
        void shouldReturn400_WhenBodyIsMissing() throws Exception {
                UUID ticketId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                String requestBody = "{}";
                mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                                .header(Constants.USER_ID, userId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getTicketById_whenRoleIsCustomer_shouldReturnTicket() throws Exception {

                UUID ticketId = UUID.randomUUID();

                CustomerTicketResponse response = new CustomerTicketResponse(
                                "Login Issue",
                                "Cannot login",
                                TicketStatus.OPEN,
                                LocalDateTime.now(),
                                "Rakshit");

                when(ticketService.getTicketForCustomerById(ticketId, customer.getId()))
                                .thenReturn(response);

                mockMvc.perform(get("/api/tickets/{id}", ticketId).header(Constants.USER_ID, customer.getId())
                                .param("role", "customer"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title").value("Login Issue"))
                                .andExpect(jsonPath("$.data.agentName").value("Rakshit"));
        }

        @Test
        void getTicketById_whenRoleIsInvalid_shouldReturnBadRequest() throws Exception {

                UUID id = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                mockMvc.perform(get("/api/tickets/{id}", id)
                                .param("role", "agent")
                                .header(Constants.USER_ID, userId))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code").value("INVALID_ROLE"))
                                .andExpect(jsonPath("$.message").value("Invalid role provided"));
        }

    @Test
    public void shouldReturn200AndTicketDetails_WhenRoleIsAgentUserAndTicketIsFound() throws Exception{

        // Given
        UUID ticketId = UUID.randomUUID();
        UUID supportAgentUserId = supportAgent.getId();

        String title = "Issue getting tickets";
        String description = "Issue must be resolved";
        TicketStatus status = TicketStatus.IN_PROGRESS;
        TicketPriority priority = TicketPriority.HIGH;
        LocalDateTime createdAt = LocalDateTime.now();

        AgentTicketResponse expectedTicket = new AgentTicketResponse(title,description,status,priority,createdAt);

        when(ticketService.getTicketByAgent(ticketId, supportAgentUserId)).thenReturn(expectedTicket);

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/tickets/{id}?role=support_agent", ticketId)
                .header(Constants.USER_ID, supportAgent.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value(expectedTicket.getTitle()))
                .andExpect(jsonPath("$.data.description").value(expectedTicket.getDescription()))
                .andExpect(jsonPath("$.data.status").value(expectedTicket.getStatus().name()))
                .andExpect(jsonPath("$.data.priority").value(expectedTicket.getPriority().name()))
                .andExpect(jsonPath("$.data.createdAt").value(expectedTicket.getCreatedAt().toString()));
    }

    @Test
    public void shouldReturn403_WhenRoleIsNotAgentOrCustomer() throws Exception{

        ResultActions resultActions = mockMvc.perform(get("/api/tickets/{id}?role=user", UUID.randomUUID())
                .header(Constants.USER_ID, supportAgent.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
        );

        resultActions
                .andExpect(status().isForbidden());
    }
}
