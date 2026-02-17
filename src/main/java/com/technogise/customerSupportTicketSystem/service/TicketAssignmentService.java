package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.*;
import com.technogise.customerSupportTicketSystem.model.TicketAssignment;
import com.technogise.customerSupportTicketSystem.repository.TicketAssignmentRepository;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
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
            throw new InvalidAssignmentException("400","Self-assignment is not valid assignment");
        }

        var ticket =ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("404","Ticket Not Found in ticket Repository"));

        if(ticket.getStatus()== TicketStatus.CLOSED){
            throw new ClosedTicketStatusException("422","Ticket Status is CLOSED, so cannot assign ticket");
        }
        var assignedByUser = userRepository.findById(assignedByUserId)
                .orElseThrow(()-> new UserNotFoundException("404","Assigned By user not found in user repository"));

        var assignedToUser = userRepository.findById(assignedToUserId)
                .orElseThrow(()-> new UserNotFoundException("404","Assigned To user not found in user repository"));

        if(assignedByUser.getRole()!= UserRole.SUPPORT_AGENT){
            throw new NonAgentRoleFoundException("403","Assigned by User is not a support agent, so cannot assign ticket");
        }
        if(assignedToUser.getRole()!= UserRole.SUPPORT_AGENT){
            throw new NonAgentRoleFoundException("403","Assigned To User is not a support agent, so cannot assign ticket");
        }
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAssignedTo(assignedToUser);
        ticketRepository.save(ticket);
        TicketAssignment ticketAssignment = new TicketAssignment();
        ticketAssignment.setTicketId(ticketId);
        ticketAssignment.setAssignedByUserId(assignedByUserId);
        ticketAssignment.setAssignedToUserId(assignedToUserId);
        TicketAssignment savedTicketAssignment = ticketAssignmentRepository.save(ticketAssignment);

        return new TicketAssignmentResponse(
                savedTicketAssignment.getId(),ticketId,assignedToUserId,assignedByUserId,"Ticket Assigned Successfully"
        );
    }

}
