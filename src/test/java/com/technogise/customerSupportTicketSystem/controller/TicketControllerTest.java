package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
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
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                        .header("User-Id", userId.toString())
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
                        .header("User", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
