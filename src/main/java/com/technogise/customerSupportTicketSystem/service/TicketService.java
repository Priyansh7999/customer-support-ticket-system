package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import org.springframework.stereotype.Service;
import com.technogise.customerSupportTicketSystem.dto.ViewTicketResponse;
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

    public CreateTicketResponse createTicket(String title, String description, UUID userId) {
        User customer = userService.getUserByIdAndRole(userId, UserRole.CUSTOMER);
        User supportAgent = userService.getRandomUserByRole(UserRole.SUPPORT_AGENT);

        Ticket ticket = new Ticket();
        ticket.setTitle(title.trim());
        ticket.setDescription(description.trim());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setCreatedBy(customer);
        ticket.setAssignedTo(supportAgent);

        Ticket createdTicket = ticketRepository.save(ticket);

        CreateTicketResponse response = new CreateTicketResponse();
        response.setId(createdTicket.getId());
        response.setTitle(createdTicket.getTitle());
        response.setDescription(createdTicket.getDescription());
        response.setStatus(createdTicket.getStatus());
        response.setAssignedToName(createdTicket.getAssignedTo().getName());
        response.setCreatedAt(createdTicket.getCreatedAt());

        return response;
    }

    public ViewTicketResponse getTicketForCustomerById(UUID id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TICKET_NOT_FOUND","Ticket not found with id: "+id));

        return new ViewTicketResponse(
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getName() : null
              
        );
    }



}
