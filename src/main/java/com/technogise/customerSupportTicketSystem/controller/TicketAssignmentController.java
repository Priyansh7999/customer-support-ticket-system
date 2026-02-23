package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentRequest;
import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentResponse;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Assignment", description = "Operations for assigning tickets to support agents")
public class TicketAssignmentController {
    private final TicketAssignmentService ticketAssignmentService;

    @Operation(summary = "Assign a ticket to an agent", description = "Allows an agent to assign or reassign a ticket to another support agent.")
    @PostMapping("/{id}/assign")
    public ResponseEntity<SuccessResponse<TicketAssignmentResponse>> assign(@PathVariable UUID id, @Valid @RequestBody TicketAssignmentRequest ticketAssignmentRequest) {
        UUID assignedByUserId = ticketAssignmentRequest.getAssignedByUserId();
        UUID assignedToUserId = ticketAssignmentRequest.getAssignedToUserId();

        TicketAssignmentResponse response = ticketAssignmentService.assignTicket(id, assignedByUserId, assignedToUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success("Ticket assigned successfully", response));
    }
}

