package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentResponse;
import com.technogise.customerSupportTicketSystem.exception.TicketNotFoundException;
import com.technogise.customerSupportTicketSystem.model.TicketAssignment;
import com.technogise.customerSupportTicketSystem.repository.TicketAssignmentRepository;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import com.technogise.customerSupportTicketSystem.exception.IllegalArgumentException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketAssignmentService {

    private final TicketAssignmentRepository ticketAssignmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    TicketAssignmentService(TicketAssignmentRepository ticketAssignmentRepository, TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketAssignmentRepository = ticketAssignmentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public TicketAssignmentResponse assignTicket(UUID ticketId, UUID assignedByUserId, UUID assignedToUserId) {
        if (assignedByUserId.equals(assignedToUserId)) {
            throw new IllegalArgumentException("400","Self-assignment is not valid assignment");
        }

        var ticket =ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("404","Ticket Not Found in User Repository"));

        TicketAssignment ticketAssignment = new TicketAssignment();
        ticketAssignment.setTicketId(ticketId);
        ticketAssignment.setAssignedByUserId(assignedByUserId);
        ticketAssignment.setAssignedToUserId(assignedToUserId);
        TicketAssignment savedTicketAssignment = ticketAssignmentRepository.save(ticketAssignment);

        return new TicketAssignmentResponse(
                savedTicketAssignment.getId(),ticketId,assignedToUserId,assignedByUserId,"Ticket Assignment Successfully"
        );
    }

}
