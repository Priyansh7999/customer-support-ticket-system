package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import org.springframework.stereotype.Service;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;

import java.util.UUID;



@Service
public class TicketService {


    private final TicketRepository ticketRepository;

    private final UserService userService;

    public TicketService(TicketRepository ticketRepository, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
    }

    public Ticket createTicket(String title, String description, UUID userId) {
        User customer = userService.getUserByIdAndRole(userId, UserRole.CUSTOMER);
        User supportAgent = userService.getRandomUserByRole(UserRole.SUPPORT_AGENT);

        Ticket createdTicket = new Ticket();
        createdTicket.setTitle(title.trim());
        createdTicket.setDescription(description.trim());
        createdTicket.setStatus(TicketStatus.OPEN);
        createdTicket.setPriority(TicketPriority.MEDIUM);
        createdTicket.setCreatedBy(customer);
        createdTicket.setAssignedTo(supportAgent);

        return ticketRepository.save(createdTicket);
    }

    public CustomerTicketResponse getTicketForCustomerById(UUID id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TICKET_NOT_FOUND","Ticket not found with id: "+id));

        return new CustomerTicketResponse(
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getName() : null
              
        );
    }



}
