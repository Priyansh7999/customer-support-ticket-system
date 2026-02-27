package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.*;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;
import com.technogise.customerSupportTicketSystem.dto.TicketView;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;

import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/tickets")
@RestControllerAdvice
@Tag(name = "Tickets", description = "Operations related to customer ticket creation, retrieval, and commenting")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @Operation(summary = "Create a new ticket", description = "Automatically assigns a support agent to the new ticket.")
    public ResponseEntity<SuccessResponse<CreateTicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal User user) {

        String title = request.getTitle();
        String description = request.getDescription();
        UUID userId = user.getId();

        CreateTicketResponse createdTicket = ticketService.createTicket(title, description, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success("Ticket created successfully", createdTicket));
    }

    @PostMapping("/{ticketId}/comments")
    @Operation(summary = "Add a comment to a ticket", description = "Allows the Agent or customer to add a comment.")
    public ResponseEntity<SuccessResponse<CreateCommentResponse>> addComment(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        UUID userId = user.getId();
        CreateCommentResponse comment = ticketService.addComment(ticketId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success("Comment added successfully",comment));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket details by ID", description = "Returns different views based on whether the user is a CUSTOMER or SUPPORT_AGENT.")
    public ResponseEntity<SuccessResponse< ? extends TicketView>> getTicketById(@PathVariable UUID id,
                                                                                @AuthenticationPrincipal User user) {

        UUID userId = user.getId();
        if (UserRole.CUSTOMER==user.getRole()) {

            CustomerTicketResponse response = ticketService.getTicketForCustomerById(id, userId);

            return ResponseEntity.ok(
                    SuccessResponse.success(
                            "Ticket fetched successfully",
                            response));
        } else if (UserRole.SUPPORT_AGENT==user.getRole()) {

            AgentTicketResponse response = ticketService.getTicketByAgent(id, userId);
            return ResponseEntity.ok(SuccessResponse.success("Ticket fetched successfully", response));
        }

        throw new InvalidUserRoleException("INVALID_ROLE", "Invalid role provided");

    }
    @Operation(summary = "Get all comments for a ticket", description = "Retrieves a list of all comments associated with a specific ticket.")
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<SuccessResponse<List<GetCommentResponse>>> getAllCommentsByTicketId(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal User user
    ) {
        List<GetCommentResponse> comments = ticketService.getAllCommentsByTicketId(ticketId, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success("Comments retrieved successfully", comments));
    }
    @PatchMapping("/{id}")
    @Operation(
            summary = "Update ticket by ID",
            description = "Customers can only update status and description. " +
                    "Support agents can update title, description, status, and priority."
    )
    public ResponseEntity<SuccessResponse<UpdateTicketResponse>> updateTicket(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateTicketRequest request) {

        UpdateTicketResponse response = ticketService.updateTicket(id, user.getId(), request);
        return ResponseEntity.ok(
                SuccessResponse.success(
                        "Ticket updated successfully",
                        response));
    }

}
