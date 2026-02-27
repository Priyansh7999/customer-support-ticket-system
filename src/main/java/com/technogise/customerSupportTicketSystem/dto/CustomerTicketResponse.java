package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTicketResponse implements TicketView{
    private UUID id;
    private String title;
    private String description;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private String agentName;
}
