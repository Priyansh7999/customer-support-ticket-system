package com.technogise.customerSupportTicketSystem.dto;

import java.time.LocalDateTime;

import com.technogise.customerSupportTicketSystem.enums.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerUpdateTicketResponse implements UpdateTicket {

   private String title;
    private String description;
    private TicketStatus status;
     private LocalDateTime createdAt;
      private LocalDateTime updatedAt;


}
