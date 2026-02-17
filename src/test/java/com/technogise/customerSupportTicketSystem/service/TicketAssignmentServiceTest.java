package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.exception.IllegalArgumentException;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserException;
import com.technogise.customerSupportTicketSystem.model.TicketAssignment;
import com.technogise.customerSupportTicketSystem.repository.TicketAssignmentRepository;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
    void shouldThrowExceptionWith404CodeAndMessage_WhenSelfAssignmentIsPerformed() {
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketAssignmentService.assignTicket(ticketId, userId, userId)
        );
        assertEquals("400",exception.getCode());
        assertEquals("Self-assignment is not valid assignment", exception.getMessage());
    }
}