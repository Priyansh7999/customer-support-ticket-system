package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CustomerTicketResponse implements TicketView{

    private String title;
    private String description;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private String agentName;

    public CustomerTicketResponse(String title,
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
}
