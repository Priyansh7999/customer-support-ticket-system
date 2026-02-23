package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.*;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
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

    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<SuccessResponse<List<GetCommentResponse>>> getAllCommentsByTicketId(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal User user
    ) {
        List<GetCommentResponse> comments = ticketService.getAllCommentsByTicketId(ticketId, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success("Comments retrieved successfully", comments));
    }
    @PatchMapping("/{id}")
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
