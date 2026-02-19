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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketAssignmentService {

    private final TicketAssignmentRepository ticketAssignmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketAssignmentService(TicketAssignmentRepository ticketAssignmentRepository,
                                   TicketRepository ticketRepository,
                                   UserRepository userRepository) {
        this.ticketAssignmentRepository = ticketAssignmentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TicketAssignmentResponse assignTicket(UUID ticketId, UUID assignedByUserId, UUID assignedToUserId) {
        validateSelfAssignment(assignedByUserId, assignedToUserId);

        Ticket ticket = fetchTicket(ticketId);
        User assignedByUser = fetchUser(assignedByUserId);
        User assignedToUser = fetchUser(assignedToUserId);

        validateAssignmentPermissions(ticket, assignedByUser, assignedToUser);

        updateTicketAssignee(ticket, assignedToUser);
        TicketAssignment savedRecord = createAssignmentAuditLog(ticket, assignedByUser, assignedToUser);

        return new TicketAssignmentResponse(
                savedRecord.getId(),
                ticketId,
                assignedToUserId,
                assignedByUserId
        );
    }

    private void validateSelfAssignment(UUID fromId, UUID toId) {
        if (fromId.equals(toId)) {
            throw new InvalidAssignmentException("BAD_REQUEST", "Self-assignment is not valid assignment");
        }
    }

    private Ticket fetchTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("NOT_FOUND", "Ticket not found with id: " + ticketId));
    }

    private User fetchUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NOT_FOUND", "User not found with id: " + userId));
    }

    private void validateAssignmentPermissions(Ticket ticket, User assignedByUser, User assignedToUser) {
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ClosedTicketStatusException("UNPROCESSABLE_ENTITY", "Ticket Status is CLOSED, so cannot assign ticket");
        }

        if (assignedByUser.getRole() != UserRole.SUPPORT_AGENT || assignedToUser.getRole() != UserRole.SUPPORT_AGENT) {
            throw new InvalidUserRoleException("FORBIDDEN", "Both users must be support agents to assign/receive tickets");
        }

        if (ticket.getAssignedTo() != null && !assignedByUser.getId().equals(ticket.getAssignedTo().getId())) {
            throw new InvalidUserRoleException("FORBIDDEN", "Only the currently assigned user can reassign this ticket");
        }
    }

    private void updateTicketAssignee(Ticket ticket, User newAssignee) {
        ticket.setAssignedTo(newAssignee);
        ticketRepository.save(ticket);
    }

    private TicketAssignment createAssignmentAuditLog(Ticket ticket, User byUser, User toUser) {
        TicketAssignment assignment = new TicketAssignment();
        assignment.setTicket(ticket);
        assignment.setAssignedByUser(byUser);
        assignment.setAssignedToUser(toUser);
        return ticketAssignmentRepository.save(assignment);
    }
}