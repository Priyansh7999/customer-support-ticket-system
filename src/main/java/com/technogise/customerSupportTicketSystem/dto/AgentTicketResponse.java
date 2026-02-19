package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;

import java.time.LocalDateTime;

public class AgentTicketResponse implements TicketView {
    String title;
    String description;
    TicketStatus status;
    TicketPriority priority;
    LocalDateTime createdAt;

    public AgentTicketResponse(String title, String description, TicketStatus ticketStatus, TicketPriority ticketPriority, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.status = ticketStatus;
        this.priority = ticketPriority;
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
