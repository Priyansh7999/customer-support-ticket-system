package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.*;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.AccessDeniedException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.technogise.customerSupportTicketSystem.dto.AgentTicketResponse;
import org.springframework.test.web.servlet.ResultActions;

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
    public GetCommentResponse mockComment;

    @BeforeEach
    void setup() {
        request = new CreateTicketRequest();
        request.setTitle("Sample Ticket");
        request.setDescription("This is a sample ticket description.");

        customer = getMockCustomer();
        supportAgent = getMockSupportAgent();
        mockTicket = getMockCreateTicketResponse();
        mockComment = getMockCommentResponse();
    }

    private User getMockCustomer() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Raj");
        user.setRole(UserRole.CUSTOMER);

        return user;
    }

    private User getMockSupportAgent() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Jatin");
        user.setRole(UserRole.SUPPORT_AGENT);

        return user;
    }

    private CreateTicketResponse getMockCreateTicketResponse() {
        CreateTicketResponse response = new CreateTicketResponse();
        response.setId(UUID.randomUUID());
        response.setTitle(request.getTitle());
        response.setDescription(request.getDescription());
        response.setStatus(TicketStatus.OPEN);
        response.setAssignedToName(supportAgent.getName());
        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    private GetCommentResponse getMockCommentResponse() {
        GetCommentResponse comment = new GetCommentResponse();
        comment.setComment("This is a sample comment.");
        comment.setCommenter(customer.getName());
        comment.setCreatedAt(LocalDateTime.now());

        return comment;
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
    void shouldReturn200AndAllComments_ForGivenTicketId() throws Exception {
        // Given
        GetCommentResponse mockComment = getMockCommentResponse();
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<GetCommentResponse> mockComments = List.of(mockComment, getMockCommentResponse());

        when(ticketService.getAllCommentsByTicketId(ticketId, userId)).thenReturn(mockComments);

        // When and Then
        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .header("User-Id", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Comments retrieved successfully"))
                .andExpect(jsonPath("$.data[0].comment").value(mockComment.getComment()))
                .andExpect(jsonPath("$.data[0].commenter").value(mockComment.getCommenter()))
                .andExpect(jsonPath("$.data[0].createdAt").value(mockComment.getCreatedAt().toString()));
    }

    @Test
    void shouldReturn404_WhenTicketNotFound() throws Exception {
        // Given
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(ticketService.getAllCommentsByTicketId(ticketId, userId))
                .thenThrow(new ResourceNotFoundException("TICKET_NOT_FOUND", "No ticket found for the provided ID."));

        // When and Then
        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .header("User-Id", userId.toString())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No ticket found for the provided ID."));
    }

    @Test
    void shouldReturn403_WhenUserNotAuthorizedToViewComments() throws Exception {
        // Given
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(ticketService.getAllCommentsByTicketId(ticketId, userId))
                .thenThrow(new AccessDeniedException("ACCESS_DENIED", "Access to this ticket is not permitted"));

        // When and Then
        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .header("User-Id", userId.toString())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Access to this ticket is not permitted"));
    }

    @Test
    void shouldReturn400AndErrorResponse_WhenUserIdHeaderIsMissing() throws Exception {
        // Given
        UUID ticketId = UUID.randomUUID();

        // When and Then
        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_USER_ID"))
                .andExpect(jsonPath("$.message").value("Missing required request header: User-Id"));
    }
}
