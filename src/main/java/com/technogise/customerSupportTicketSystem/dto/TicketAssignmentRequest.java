package com.technogise.customerSupportTicketSystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignmentRequest{

    @NotNull(message ="Assigned by user ID is required")
    private UUID assignedByUserId;

    @NotNull(message ="Assigned to user ID is required")
    private UUID assignedToUserId;
}
