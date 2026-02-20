package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;

import java.time.LocalDateTime;

public class TicketResponse {
    String title;
    String description;
    TicketStatus status;
    TicketPriority priority;
    LocalDateTime createdAt;
}
