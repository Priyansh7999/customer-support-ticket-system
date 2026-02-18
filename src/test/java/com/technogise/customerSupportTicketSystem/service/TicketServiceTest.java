package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.AgentTicketResponse;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.AccessDeniedException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.Comment;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import com.technogise.customerSupportTicketSystem.repository.CommentRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;

import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private CreateTicketRequest mockTicketRequest;
    private User customer;
    private User supportAgent;
    private CreateTicketResponse mockTicketResponse;
    private CreateTicketRequest request;
    private User testUser;



    @BeforeEach
    void setup() {
        mockTicketRequest = new CreateTicketRequest();
        mockTicketRequest.setTitle("Sample Ticket");
        mockTicketRequest.setDescription("This is a sample ticket description.");

        customer = getMockCustomer();
        supportAgent = getMockSupportAgent();
        mockTicketResponse = getMockCreateTicketResponse();
    }

    @Test
    void shouldReturnTicket_WhenTicketCreatedSuccessfully() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());

        // When
        CreateTicketResponse result = ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        assertEquals(mockTicketRequest.getTitle(), result.getTitle());
        assertEquals(mockTicketRequest.getDescription(), result.getDescription());
    }

    @Test
    void shouldSetDefaultStatusToOpen_WhenTicketCreatedSuccessfully() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        assertEquals(TicketStatus.OPEN, savedTicket.getStatus());
    }

    @Test
    void shouldSetDefaultPriorityToMedium_WhenTicketCreatedSuccessfully() {
        // When
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        verify(ticketRepository).save(argThat(ticket -> ticket.getPriority() == TicketPriority.MEDIUM));
    }

    @Test
    void shouldSetCreatedBy_WhenUserExistsAndRoleIsCustomer() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        when(userService.getUserByIdAndRole(customer.getId(), UserRole.CUSTOMER)).thenReturn(customer);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        assertEquals(customer.getId(), ticketCaptor.getValue().getCreatedBy().getId());
        assertEquals(UserRole.CUSTOMER, ticketCaptor.getValue().getCreatedBy().getRole());
    }

    @Test
    void shouldSetAssignedTo_WhenUserExistsAndRoleIsSupportAgent() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        when(userService.getRandomUserByRole(UserRole.SUPPORT_AGENT)).thenReturn(supportAgent);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), supportAgent.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        assertEquals(UserRole.SUPPORT_AGENT, ticketCaptor.getValue().getAssignedTo().getRole());
    }

    private User getMockCustomer() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Mock User");
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
        response.setTitle(mockTicketRequest.getTitle());
        response.setDescription(mockTicketRequest.getDescription());
        response.setStatus(TicketStatus.OPEN);
        response.setAssignedToName("Jatin");
        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    private Ticket getMockTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTitle(mockTicketRequest.getTitle());
        ticket.setDescription(mockTicketRequest.getDescription());
        ticket.setAssignedTo(supportAgent);

        return ticket;
    }

    @Test
    void shouldReturnComment_WhenCommentIsAddedSuccessfully() {
        // Given
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        User agentUser = new User();
        agentUser.setId(agentId);
        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setCreatedBy(user);
        ticket.setAssignedTo(agentUser);
        ticket.setStatus(TicketStatus.OPEN);
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("hi i am priyansh");

        Comment savedComment = new Comment();
        savedComment.setId(UUID.randomUUID());
        savedComment.setBody("hi i am priyansh");
        savedComment.setCommenter(user);
        savedComment.setTicket(ticket);
        savedComment.setCreatedAt(LocalDateTime.now());
        savedComment.setUpdatedAt(LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // When
        CreateCommentResponse result = ticketService.addComment(ticketId, request, userId);

        // Then
        assertNotNull(result);
        assertEquals(savedComment.getId(), result.getId());
        assertEquals(savedComment.getBody(), result.getBody());
        assertEquals(savedComment.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void shouldThrowException_WhenUserIsNotPresent() {
        // Given
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("Test comment");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // When
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.addComment(ticketId, request, userId)
        );
        // Then
        assertEquals("User not found with id: " + userId, exception.getMessage());
    }
    @Test
    void shouldThrowException_WhenTicketIsNotPresent() {
        // Given
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("Test comment");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.addComment(ticketId, request, userId)
        );

        // Then
        assertEquals("Ticket not found with id: " + ticketId, exception.getMessage());
    }

    @Test
    void shouldThrowException_WhenUserNotBelongToTicket() {
        UUID ticketId = UUID.randomUUID();
        UUID ticketAgentId = UUID.randomUUID();
        UUID ticketOwnerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User user = new User();
        user.setId(ticketOwnerId);
        User agent = new User();
        agent.setId(ticketAgentId);
        User otherUser = new User();
        otherUser.setId(otherUserId);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setCreatedBy(user);
        ticket.setAssignedTo(agent);

        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("Test comment");

        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                ()->ticketService.addComment(ticketId,request,otherUserId)
        );
        assertEquals("This ticket does not belongs to you", exception.getMessage());
    }

    void shouldReturnTicket_whenTicketExists() {

        UUID id = UUID.randomUUID();
          UUID userId = UUID.randomUUID();

        User agent = new User();
        agent.setName("Rakshit");

        Ticket ticket = new Ticket();
        ticket.setTitle("Login Issue");
        ticket.setDescription("Cannot login");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setAssignedTo(agent);
         ticket.setCreatedBy(customer);

        when(ticketRepository.findById(id))
                .thenReturn(Optional.of(ticket));
        when(userService.getUserByIdAndRole(userId, UserRole.CUSTOMER))
            .thenReturn(customer);

        CustomerTicketResponse response = ticketService.getTicketForCustomerById(id,userId);

        assertEquals("Login Issue", response.getTitle());
        assertEquals("Rakshit", response.getAgentName());
    }

    @Test
    void shouldThrowException_whenTicketNotFound() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(ticketRepository.findById(id))
                .thenReturn(Optional.empty());
        when(userService.getUserByIdAndRole(userId, UserRole.CUSTOMER))
            .thenReturn(customer);

                ResourceNotFoundException exception =
            assertThrows(ResourceNotFoundException.class,
                    () -> ticketService.getTicketForCustomerById(id,userId));

                     assertEquals("TICKET_NOT_FOUND", exception.getCode());
    }

    @Test
    void getTicketByAgentUser_shouldReturnTicketDetailsForAgentUser_WhenTicketExists() {

        // Given
        UUID id = UUID.randomUUID();
        String title = "Issue getting tickets";
        String description = "Issue must be resolved";
        TicketStatus status = TicketStatus.IN_PROGRESS;
        TicketPriority priority = TicketPriority.HIGH;
        LocalDateTime createdAt = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCreatedAt(createdAt);

        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));

        // When
        AgentTicketResponse response =
                ticketService.getTicketByAgentUser(id);

        // Then
        assertEquals(ticket.getTitle(), response.getTitle());
        assertEquals(ticket.getDescription(), response.getDescription());
        assertEquals(ticket.getStatus(), response.getStatus());
        assertEquals(ticket.getPriority(), response.getPriority());
        assertEquals(ticket.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void getTicketByAgentUser_shouldThrowResourceNotFoundError_whenNonExistingTicketIdIsPassed() {

        // Given
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        // Then
        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketByAgentUser(id);
        });
    }
}

    @Test
    void  shouldThrowExceptionForbidden_whenCustomerDoesNotOwnTicket() {

    UUID ticketId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();


    User customer = new User();
    customer.setId(userId);


    User otherCustomer = new User();
    otherCustomer.setId(UUID.randomUUID());

    Ticket ticket = new Ticket();
    ticket.setCreatedBy(otherCustomer);

    when(userService.getUserByIdAndRole(userId, UserRole.CUSTOMER))
            .thenReturn(customer);

    when(ticketRepository.findById(ticketId))
            .thenReturn(Optional.of(ticket));

    AccessDeniedException exception =
            assertThrows(AccessDeniedException.class,
                    () -> ticketService.getTicketForCustomerById(ticketId, userId));

    assertEquals("FORBIDDEN", exception.getCode());
}



}

