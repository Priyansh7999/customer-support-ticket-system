package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.exception.ClosedTicketStatusException;
import com.technogise.customerSupportTicketSystem.exception.IllegalArgumentException;
import com.technogise.customerSupportTicketSystem.exception.TicketNotFoundException;
import com.technogise.customerSupportTicketSystem.model.Ticket;
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

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
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

        TicketNotFoundException ex = assertThrows(
                TicketNotFoundException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("404",ex.getCode());
        assertEquals("Ticket Not Found in User Repository", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWithStatusCode422AndExceptionMessage_whenTicketClosed() {
        UUID ticketId = UUID.randomUUID();
        UUID assignBy = UUID.randomUUID();
        UUID assignTo = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CLOSED);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ClosedTicketStatusException ex = assertThrows(
                ClosedTicketStatusException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, assignBy, assignTo)
        );
        assertEquals("422",ex.getCode());
        assertEquals("Ticket Status is CLOSED, so cannot assign ticket", ex.getMessage());
    }

}