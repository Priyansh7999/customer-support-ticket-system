package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.*;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.AccessDeniedException;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.technogise.customerSupportTicketSystem.repository.CommentRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;

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

    @BeforeEach
    void setup() {
        mockTicketRequest = new CreateTicketRequest();
        mockTicketRequest.setTitle("Sample Ticket");
        mockTicketRequest.setDescription("This is a sample ticket description.");

        customer = getMockCustomer();
        supportAgent = getMockSupportAgent();
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

    private Ticket getMockTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTitle(mockTicketRequest.getTitle());
        ticket.setDescription(mockTicketRequest.getDescription());
        ticket.setAssignedTo(supportAgent);

        return ticket;
    }

    private Comment getMockComment() {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setBody("This is a sample comment.");
        comment.setCommenter(customer);
        comment.setTicket(getMockTicket());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        return comment;
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
        assertEquals("No ticket found for the provided ID.", exception.getMessage());
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
                () -> ticketService.addComment(ticketId, request, otherUserId)
        );
        assertEquals("Access to this ticket is not permitted", exception.getMessage());
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

        CustomerTicketResponse response = ticketService.getTicketForCustomerById(id, userId);

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
                        () -> ticketService.getTicketForCustomerById(id, userId));

        assertEquals("NOT_FOUND", exception.getCode());
    }

    @Test
    void shouldReturnTicketDetails_WhenTicketExistsForAgentUser() {

        // Given
        UUID ticketId = UUID.randomUUID();
        UUID supportAgentUserId = supportAgent.getId();

        String title = "Issue getting tickets";
        String description = "Issue must be resolved";
        TicketStatus status = TicketStatus.IN_PROGRESS;
        TicketPriority priority = TicketPriority.HIGH;
        LocalDateTime createdAt = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCreatedAt(createdAt);

        when(userService.getUserByIdAndRole(supportAgent.getId(), UserRole.SUPPORT_AGENT)).thenReturn(supportAgent);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // When
        AgentTicketResponse response =
                ticketService.getTicketByAgent(ticketId, supportAgentUserId);

        // Then
        assertEquals(ticket.getTitle(), response.getTitle());
        assertEquals(ticket.getDescription(), response.getDescription());
        assertEquals(ticket.getStatus(), response.getStatus());
        assertEquals(ticket.getPriority(), response.getPriority());
        assertEquals(ticket.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void shouldThrowResourceNotFoundError_WhenAgentUserAccessesNonExistingTicket() {

        // Given
        UUID ticketId = UUID.randomUUID();
        UUID supportAgentUserId = supportAgent.getId();

        when(userService.getUserByIdAndRole(supportAgent.getId(), UserRole.SUPPORT_AGENT)).thenReturn(supportAgent);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Then
        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketByAgent(ticketId, supportAgentUserId);
        });
    }

    @Test
    void shouldThrowUnauthorizedError_WhenUserAccessingAgentRelatedTicketDetailsIsNotAgent() {

        // Given
        UUID ticketId = UUID.randomUUID();
        UUID customerUserId = customer.getId();

        String title = "Issue getting tickets";
        String description = "Issue must be resolved";
        TicketStatus status = TicketStatus.IN_PROGRESS;
        TicketPriority priority = TicketPriority.HIGH;
        LocalDateTime createdAt = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCreatedAt(createdAt);
        ticket.setCreatedBy(customer);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketService.getTicketByAgent(ticketId, customerUserId))
                .thenThrow(
                        new InvalidUserRoleException(
                                "FORBIDDEN", "User is not authorized to perform this action. Required role: "
                                + UserRole.SUPPORT_AGENT));

        // Then
        assertThrows(InvalidUserRoleException.class, () -> {
            ticketService.getTicketByAgent(ticketId, customerUserId);
        });
    }
    @Test
    void shouldRetrieveAllComments_ForValidTicketId() {
        // Given
        Comment mockComment = getMockComment();
        Ticket ticket = getMockTicket();
        UUID ticketId = ticket.getId();
        UUID userId = mockComment.getCommenter().getId();
        List<Comment> mockComments = List.of(mockComment, getMockComment());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(commentRepository.findAllByTicketId(ticketId)).thenReturn(mockComments);

        // When
        List<GetCommentResponse> result = ticketService.getAllCommentsByTicketId(ticketId, userId);

        // Then
        assertNotNull(result);
        assertEquals(mockComments.size(), result.size());
        assertEquals(mockComments.getFirst().getBody(), result.getFirst().getComment());
        assertEquals(mockComments.getFirst().getCommenter().getName(), result.getFirst().getCommenter());
        assertEquals(mockComments.getFirst().getCreatedAt(), result.getFirst().getCreatedAt());

        assertEquals(mockComments.getLast().getBody(), result.getLast().getComment());
        assertEquals(mockComments.getLast().getCommenter().getName(), result.getLast().getCommenter());
        assertEquals(mockComments.getLast().getCreatedAt(), result.getLast().getCreatedAt());
    }

    @Test
    void shouldThrowExceptionForbidden_whenCustomerDoesNotOwnTicket() {

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

    @Test
    void shouldReturnEmptyList_WhenNoCommentsFound_ForGivenTicketId() {
        // Given
        Comment mockComment = getMockComment();
        Ticket ticket = getMockTicket();
        UUID ticketId = ticket.getId();
        UUID userId = mockComment.getCommenter().getId();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(commentRepository.findAllByTicketId(ticketId)).thenReturn(List.of());

        // When
        List<GetCommentResponse> result = ticketService.getAllCommentsByTicketId(ticketId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowException_WhenUserNotAuthorizedToViewComments() {
        // Given
        Ticket ticket = getMockTicket();
        UUID ticketId = ticket.getId();
        UUID unauthorizedUserId = UUID.randomUUID();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // When
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> ticketService.getAllCommentsByTicketId(ticketId, unauthorizedUserId)
        );

        // Then
        assertEquals("ACCESS_DENIED", exception.getCode());
        assertEquals("Access to this ticket is not permitted", exception.getMessage());
    }
}
