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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAssignmentServiceTest {
    @Mock
    private TicketAssignmentRepository ticketAssignmentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketAssignmentService ticketAssignmentService;

    @Test
    void shouldThrowExceptionWith404CodeAndExceptionMessage_WhenSelfAssignmentIsPerformed() {
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        InvalidAssignmentException exception = assertThrows(
                InvalidAssignmentException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, userId, userId)
        );
        assertEquals("400",exception.getCode());
        assertEquals("Self-assignment is not valid assignment", exception.getMessage());
    }


    @Test
    void shouldThrowExceptionWithCode404AndExceptionMessage_whenTicketNotFoundInRepository() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("404",exception.getCode());
        assertEquals("Ticket not found with id: "+ticketId, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWithStatusCode422AndExceptionMessage_whenTicketClosed() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CLOSED);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ClosedTicketStatusException exception = assertThrows(
                ClosedTicketStatusException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("422",exception.getCode());
        assertEquals("Ticket Status is CLOSED, so cannot assign ticket", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWithStatusCode404AndExceptionMessage_whenAssignedByUserNotFound() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("404",exception.getCode());
        assertEquals("User not found with id: "+assignBy, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWithStatusCode404AndExceptionMessage_whenAssignedToUserNotFound() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(assignTo)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("404",exception.getCode());
        assertEquals("User not found with id: "+assignTo, exception.getMessage());
    }
    @Test
    void shouldThrowExceptionWithStatusCode403AndExceptionMessage_whenAssignedByUserNotSupportAgent() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User user = new User();
        user.setRole(UserRole.CUSTOMER);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(user));
        when(userRepository.findById(assignTo)).thenReturn(Optional.of(new User()));

        InvalidUserRoleException exception = assertThrows(
                InvalidUserRoleException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("403",exception.getCode());
        assertEquals("Assigned by User is not a support agent, so cannot assign ticket", exception.getMessage());
    }
    @Test
    void shouldThrowExceptionWithStatusCode403AndExceptionMessage_whenAssignedToUserNotSupportAgent() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User user = new User();
        user.setRole(UserRole.CUSTOMER);
        User supportUser = new User();
        supportUser.setRole(UserRole.SUPPORT_AGENT);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(supportUser));
        when(userRepository.findById(assignTo)).thenReturn(Optional.of(user));

        InvalidUserRoleException exception = assertThrows(
                InvalidUserRoleException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("403",exception.getCode());
        assertEquals("Assigned To User is not a support agent, so cannot assign ticket", exception.getMessage());
    }


    @Test
    void shouldAssertEqualAndAssignTicketSuccessfully_WhenUsersAreSupportAgentsAndTicketExists() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);

        User agent = new User();
        agent.setId(assignBy);
        agent.setRole(UserRole.SUPPORT_AGENT);

        TicketAssignment savedAssignment = new TicketAssignment();
        savedAssignment.setId(UUID.randomUUID());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignBy)).thenReturn(Optional.of(agent));
        when(userRepository.findById(assignTo)).thenReturn(Optional.of(agent));
        when(ticketRepository.save(any())).thenReturn(ticket);
        when(ticketAssignmentRepository.save(any())).thenReturn(savedAssignment);

        TicketAssignmentResponse response =
                ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo);

        assertNotNull(response);
        assertEquals(ticketId, response.getTicketId());
        assertEquals(assignTo, response.getAssignedToUserId());
        assertEquals(assignBy, response.getAssignedByUserId());
        assertEquals("Ticket Assigned Successfully", response.getMessage());
    }

}