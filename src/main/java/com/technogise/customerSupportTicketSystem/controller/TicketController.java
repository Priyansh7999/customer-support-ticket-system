package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import org.springframework.web.bind.annotation.*;


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
            @RequestHeader (Constants.USER_ID)UUID userId) {

        String title = request.getTitle();
        String description = request.getDescription();

        CreateTicketResponse createdTicket = ticketService.createTicket(title, description, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success("Ticket created successfully", createdTicket));
    }


    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<SuccessResponse<CreateCommentResponse>> addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CreateCommentRequest request,
            @RequestHeader("User-Id") UUID userId) {
        CreateCommentResponse comment = ticketService.addComment(ticketId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success("Comment added successfully",comment));
    }
}
