package com.technogise.customerSupportTicketSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignmentResponse {
    private UUID id;
    private UUID ticketId;
    private UUID assignedToUserId;
    private UUID assignedByUserId;
    private String message;
}