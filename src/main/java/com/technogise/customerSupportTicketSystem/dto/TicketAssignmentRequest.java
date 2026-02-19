package com.technogise.customerSupportTicketSystem.dto;

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

    @NotNull(message ="valid assignByUserId is required")
    private UUID assignedByUserId;

    @NotNull(message ="valid assignToUserId is required")
    private UUID assignedToUserId;
}
