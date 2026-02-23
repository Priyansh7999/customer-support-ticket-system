package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.config.SecurityConfig;
import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.*;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import com.technogise.customerSupportTicketSystem.dto.UpdateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.UpdateTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.AccessDeniedException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import com.technogise.customerSupportTicketSystem.service.JwtService;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import com.technogise.customerSupportTicketSystem.dto.AgentTicketResponse;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(TicketController.class)
@Import(SecurityConfig.class)
public class TicketControllerTest {

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

    private CreateTicketRequest request;
    private User customer;
    private User supportAgent;
    private CreateTicketResponse mockTicket;
    private GetCommentResponse mockComment;

    @BeforeEach
    void setup() {
        request = new CreateTicketRequest();
        request.setTitle("Sample Ticket");
        request.setDescription("This is a sample ticket description.");

        customer = getMockCustomer();
        supportAgent = getMockSupportAgent();
        mockTicket = getMockCreateTicketResponse();
        mockComment = getMockCommentResponse();

        authFor(customer);
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


    private Authentication authFor(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void shouldReturn201AndTicket_WhenCreateTicketSuccessfully() throws Exception {
        when(ticketService.createTicket(request.getTitle(), request.getDescription(), customer.getId()))
                .thenReturn(mockTicket);

        mockMvc.perform(post("/api/tickets")
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ticket created successfully"))
                .andExpect(jsonPath("$.data.title").value("Sample Ticket"))
                .andExpect(jsonPath("$.data.description").value("This is a sample ticket description."));
    }

    @Test
    void shouldReturn400_WhenTitleIsNull() throws Exception {
        request.setTitle(null);

        mockMvc.perform(post("/api/tickets")
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                .andExpect(jsonPath("$.message").value("Title is required"));
    }

    @Test
    void shouldReturn400_WhenDescriptionIsNull() throws Exception {
        request.setDescription(null);

        mockMvc.perform(post("/api/tickets")
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                .andExpect(jsonPath("$.message").value("Description is required"));
    }

    @Test
    void shouldReturn401_WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void shouldReturn400_WhenTitleExceedsMaxLength() throws Exception {
        request.setTitle("A".repeat(101));

        mockMvc.perform(post("/api/tickets")
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                .andExpect(jsonPath("$.message").value("Title must not exceed 100 characters"));
    }

    @Test
    void shouldReturn400_WhenDescriptionExceedsMaxLength() throws Exception {
        request.setDescription("A".repeat(1001));


        mockMvc.perform(post("/api/tickets")
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATA_FIELD"))
                .andExpect(jsonPath("$.message").value("Description must not exceed 1000 characters"));
    }
    @Test
    void shouldReturn201_WhenCommentAddedSuccessfully() throws Exception {
        UUID ticketId = UUID.randomUUID();

        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setBody("comment");

        CreateCommentResponse response = new CreateCommentResponse();
        response.setBody("comment");

        when(ticketService.addComment(eq(ticketId), any(CreateCommentRequest.class), eq(customer.getId())))
                .thenReturn(response);

        mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.body").value("comment"));
    }

    @Test
    void shouldReturn400_WhenBodyIsMissing() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
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

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .with(authentication(authFor(customer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Login Issue"))
                .andExpect(jsonPath("$.data.agentName").value("Rakshit"));
    }

    @Test
    public void shouldReturn200AndTicketDetails_WhenRoleIsAgentAndTicketIsFound() throws Exception {
        UUID ticketId = UUID.randomUUID();

        AgentTicketResponse expectedTicket = new AgentTicketResponse(
                "Issue getting tickets",
                "Issue must be resolved",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH,
                LocalDateTime.now());

        when(ticketService.getTicketByAgent(ticketId, supportAgent.getId()))
                .thenReturn(expectedTicket);

        ResultActions resultActions = mockMvc.perform(get("/api/tickets/{id}", ticketId)
                .with(authentication(authFor(supportAgent)))
                .contentType(MediaType.APPLICATION_JSON));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value(expectedTicket.getTitle()))
                .andExpect(jsonPath("$.data.description").value(expectedTicket.getDescription()))
                .andExpect(jsonPath("$.data.status").value(expectedTicket.getStatus().name()))
                .andExpect(jsonPath("$.data.priority").value(expectedTicket.getPriority().name()));
    }

    @Test
    void getTicketById_whenRoleIsInvalid_shouldReturnForbidden() throws Exception {
        User unknownRoleUser = new User();
        unknownRoleUser.setId(UUID.randomUUID());
        unknownRoleUser.setName("Unknown");
        unknownRoleUser.setRole(UserRole.CUSTOMER);

        when(ticketService.getTicketForCustomerById(any(), any()))
                .thenThrow(new InvalidUserRoleException("INVALID_ROLE", "Invalid role provided"));

        mockMvc.perform(get("/api/tickets/{id}", UUID.randomUUID())
                        .with(authentication(authFor(unknownRoleUser))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INVALID_ROLE"))
                .andExpect(jsonPath("$.message").value("Invalid role provided"));
    }

    @Test
    void shouldReturn200AndAllComments_ForGivenTicketId() throws Exception {
        UUID ticketId = UUID.randomUUID();
        List<GetCommentResponse> mockComments = List.of(mockComment, getMockCommentResponse());

        when(ticketService.getAllCommentsByTicketId(ticketId, customer.getId()))
                .thenReturn(mockComments);

        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .with(authentication(authFor(customer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Comments retrieved successfully"))
                .andExpect(jsonPath("$.data[0].comment").value(mockComment.getComment()))
                .andExpect(jsonPath("$.data[0].commenter").value(mockComment.getCommenter()))
                .andExpect(jsonPath("$.data[0].createdAt").value(mockComment.getCreatedAt().toString()));
    }

    @Test
    void shouldReturn404_WhenTicketNotFound() throws Exception {
        UUID ticketId = UUID.randomUUID();

        when(ticketService.getAllCommentsByTicketId(ticketId, customer.getId()))
                .thenThrow(new ResourceNotFoundException("TICKET_NOT_FOUND", "No ticket found for the provided ID."));

        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .with(authentication(authFor(customer))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No ticket found for the provided ID."));
    }

    @Test
    void shouldReturn403_WhenUserNotAuthorizedToViewComments() throws Exception {
        UUID ticketId = UUID.randomUUID();

        when(ticketService.getAllCommentsByTicketId(ticketId, customer.getId()))
                .thenThrow(new AccessDeniedException("ACCESS_DENIED", "Access to this ticket is not permitted"));

        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .with(authentication(authFor(customer))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Access to this ticket is not permitted"));
    }

    @Test
    public void shouldReturn200_WhenTicketUpdatedSuccessfully() throws Exception {

        UUID ticketId = UUID.randomUUID();

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setDescription("Updated description");
        request.setStatus(TicketStatus.CLOSED);

        UpdateTicketResponse response = new UpdateTicketResponse(
                "Sample Title",
                "Updated description",
                TicketStatus.CLOSED,
                TicketPriority.HIGH,
                LocalDateTime.now(),
                LocalDateTime.now());

        System.out.println(response.getTitle());

        when(ticketService.updateTicket(eq(ticketId), eq(customer.getId()), any(UpdateTicketRequest.class)))
                .thenReturn(response);

        ResultActions result = mockMvc.perform(
                patch("/api/tickets/{id}", ticketId)
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Sample Title"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    public void shouldReturn400_WhenInvalidStatusValuePassed() throws Exception {

        UUID ticketId = UUID.randomUUID();

        String invalidRequestJson = """
                {
                    "status": "INVALID_STATUS"
                }
                """;

        ResultActions resultActions = mockMvc.perform(
                patch("/api/tickets/{id}", ticketId)
                        .with(authentication(authFor(customer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson));

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ENUM_VALUE"));
    }

    @Test
    void shouldReturn200WithUpdatedTicket_WhenSupportAgentUpdatesTheirAssignedTicket() throws Exception {

        // Given
        UUID ticketId = UUID.randomUUID();
        UUID agentId = supportAgent.getId();

        UpdateTicketRequest updateRequest = new UpdateTicketRequest();
        updateRequest.setStatus(TicketStatus.CLOSED);
        updateRequest.setPriority(TicketPriority.HIGH);

        UpdateTicketResponse updateResponse = new UpdateTicketResponse(
                "Sample Title",
                "Updated description",
                TicketStatus.CLOSED,
                TicketPriority.HIGH,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(ticketService.updateTicket(eq(ticketId), eq(agentId), any(UpdateTicketRequest.class)))
                .thenReturn(updateResponse);

        // When & Then
        mockMvc.perform(patch("/api/tickets/{id}", ticketId)
                        .with(authentication(authFor(supportAgent)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ticket updated successfully"))
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void shouldReturn403_WhenSupportAgentUpdatesATicketIsNotAssignedToSameAgent() throws Exception {

        // Given
        UUID ticketId = UUID.randomUUID();
        UUID agentId = supportAgent.getId();

        UpdateTicketRequest updateRequest = new UpdateTicketRequest();
        updateRequest.setStatus(TicketStatus.CLOSED);
        updateRequest.setPriority(TicketPriority.HIGH);

        when(ticketService.updateTicket(eq(ticketId), eq(agentId), any(UpdateTicketRequest.class)))
                .thenThrow(new AccessDeniedException(
                        "FORBIDDEN",
                        "You can only update tickets assigned to you"));

        // When & Then
        mockMvc.perform(patch("/api/tickets/{id}", ticketId)
                        .with(authentication(authFor(supportAgent)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You can only update tickets assigned to you"));
    }
}