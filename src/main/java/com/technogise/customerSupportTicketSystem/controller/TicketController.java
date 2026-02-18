package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<CreateTicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @RequestHeader ("User-Id")UUID userId) {

        String title = request.getTitle();
        String description = request.getDescription();

        Ticket createdTicket = ticketService.createTicket(title, description, userId);

        CreateTicketResponse response = new CreateTicketResponse();
        response.setId(createdTicket.getId());
        response.setTitle(createdTicket.getTitle());
        response.setDescription(createdTicket.getDescription());
        response.setStatus(createdTicket.getStatus());
        response.setAssignedToName(createdTicket.getAssignedTo().getName());
        response.setCreatedAt(createdTicket.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success("Ticket created successfully", response));
    }
}
