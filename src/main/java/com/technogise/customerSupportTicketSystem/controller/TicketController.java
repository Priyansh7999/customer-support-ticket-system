package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.*;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import org.springframework.web.bind.annotation.*;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;
import com.technogise.customerSupportTicketSystem.dto.TicketView;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;

import java.util.UUID;

import com.technogise.customerSupportTicketSystem.exception.BadRequestException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/tickets")
@RestControllerAdvice
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

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse< ? extends TicketView>> getTicketById(@PathVariable UUID id,
            @RequestParam String role,@RequestHeader(Constants.USER_ID) UUID userId) {

        if (UserRole.CUSTOMER.toString().equalsIgnoreCase(role)) {

            CustomerTicketResponse response = ticketService.getTicketForCustomerById(id, userId);

            return ResponseEntity.ok(
                    SuccessResponse.success(
                            "Ticket fetched successfully",
                            response));
        } else if (UserRole.SUPPORT_AGENT.toString().equalsIgnoreCase(role)) {

            AgentTicketResponse response = ticketService.getTicketByAgent(id, userId);
            return ResponseEntity.ok(SuccessResponse.success("Ticket fetched successfully", response));
        }

        throw new InvalidUserRoleException("INVALID_ROLE", "Invalid role provided");

    }
}
