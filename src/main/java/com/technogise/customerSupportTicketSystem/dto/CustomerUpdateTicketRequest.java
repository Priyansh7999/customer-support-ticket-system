package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.TicketStatus;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CustomerUpdateTicketRequest implements UpdateTicket{

    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Pattern(regexp = "^(?!\\d+$).+", message = "Description must be a string and should contain letters")
    private String description;
    
    private TicketStatus status;

}