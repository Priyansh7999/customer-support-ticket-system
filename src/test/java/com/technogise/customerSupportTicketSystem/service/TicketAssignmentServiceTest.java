package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.*;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.model.TicketAssignment;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.TicketAssignmentRepository;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketAssignmentServiceTest {

    @Mock private TicketAssignmentRepository ticketAssignmentRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TicketAssignmentService ticketAssignmentService;

    private UUID ticketId;
    private UUID assignBy;
    private UUID assignTo;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        assignBy = UUID.randomUUID();
        assignTo = UUID.randomUUID();
    }

    @Test
    void shouldThrowException_WhenSelfAssignmentIsPerformed() {
        InvalidAssignmentException exception = assertThrows(
                InvalidAssignmentException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignBy)
        );
        assertEquals("BAD_REQUEST", exception.getCode());
        assertEquals("Self-assignment is not valid assignment", exception.getMessage());
    }

    @Test
    void shouldThrowException_WhenTicketNotFound() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("NOT_FOUND", exception.getCode());
    }

    @Test
    void shouldThrowException_WhenTicketIsClosed() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CLOSED);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(assignTo)).thenReturn(Optional.of(new User()));

        ClosedTicketStatusException exception = assertThrows(
                ClosedTicketStatusException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("UNPROCESSABLE_ENTITY", exception.getCode());
        assertEquals("Ticket Status is CLOSED, so cannot assign ticket", exception.getMessage());
    }

    @Test
    void shouldThrowException_WhenUsersAreNotSupportAgents() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User customer = new User();
        customer.setRole(UserRole.CUSTOMER);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(customer));
        when(userRepository.findById(assignTo)).thenReturn(Optional.of(new User()));

        InvalidUserRoleException exception = assertThrows(
                InvalidUserRoleException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("FORBIDDEN", exception.getCode());
        assertEquals("Both users must be support agents to assign/receive tickets", exception.getMessage());
    }

    @Test
    void shouldThrowException_WhenAssignerIsNotTheCurrentlyAssignedUser() {
        User currentlyAssigned = new User();
        currentlyAssigned.setId(UUID.randomUUID()); // Different from assignBy

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignedTo(currentlyAssigned);

        User assigner = new User();
        assigner.setId(assignBy);
        assigner.setRole(UserRole.SUPPORT_AGENT);

        User receiver = new User();
        receiver.setRole(UserRole.SUPPORT_AGENT);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(assigner));
        when(userRepository.findById(assignTo)).thenReturn(Optional.of(receiver));

        InvalidUserRoleException exception = assertThrows(
                InvalidUserRoleException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("Only the currently assigned user can reassign this ticket", exception.getMessage());
    }

    @Test
    void shouldAssignTicketSuccessfully() {
        User agentBy = new User();
        agentBy.setId(assignBy);
        agentBy.setRole(UserRole.SUPPORT_AGENT);

        User agentTo = new User();
        agentTo.setId(assignTo);
        agentTo.setRole(UserRole.SUPPORT_AGENT);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignedTo(agentBy); // Assigner is current owner

        TicketAssignment savedAssignment = new TicketAssignment();
        savedAssignment.setId(UUID.randomUUID());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(agentBy));
        when(userRepository.findById(assignTo)).thenReturn(Optional.of(agentTo));
        when(ticketAssignmentRepository.save(any())).thenReturn(savedAssignment);

        TicketAssignmentResponse response = ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo);

        assertNotNull(response);
        verify(ticketRepository, times(1)).save(ticket);
        verify(ticketAssignmentRepository, times(1)).save(any(TicketAssignment.class));
    }
}