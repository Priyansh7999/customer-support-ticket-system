package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentRequest;
import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentResponse;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketAssignmentController {
    private final TicketAssignmentService ticketAssignmentService;

    @PostMapping("/{id}/assign")
    public ResponseEntity<SuccessResponse<TicketAssignmentResponse>> assign(@PathVariable UUID id, @Valid @RequestBody TicketAssignmentRequest ticketAssignmentRequest) {
        UUID assignedByUserId = ticketAssignmentRequest.getAssignedByUserId();
        UUID assignedToUserId = ticketAssignmentRequest.getAssignedToUserId();

        TicketAssignmentResponse response = ticketAssignmentService.assignTicket(id, assignedByUserId, assignedToUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success("Ticket assigned successfully", response));
    }
}

