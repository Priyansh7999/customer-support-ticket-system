package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.ViewTicketResponse;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.technogise.customerSupportTicketSystem.exception.InvalidRoleException;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ViewTicketResponse>> getTicketById(@PathVariable UUID id,
            @RequestParam String role) {

        if ("customer".equalsIgnoreCase(role)) {

            ViewTicketResponse response = ticketService.getTicketForCustomerById(id);

            return ResponseEntity.ok(
                    SuccessResponse.success(
                            "Ticket fetched successfully",
                            response));
        }

        throw new InvalidRoleException("INVALID_ROLE", "Invalid role provided");

}

}