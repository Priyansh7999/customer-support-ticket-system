package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import java.time.LocalDateTime;

public class ViewTicketResponse {

    private String title;
    private String description;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private String agentName;

    public ViewTicketResponse(String title,
            String description,
            TicketStatus status,
            LocalDateTime createdAt,
            String agentName) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.agentName = agentName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAgentName() {
        return agentName;
    }
}
