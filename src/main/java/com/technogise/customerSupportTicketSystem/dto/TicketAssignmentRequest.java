package com.technogise.customerSupportTicketSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @NotNull(message ="assigner ID (assignByUserId) is required")
    @Schema(description = "UUID of the agent performing the assignment")
    private UUID assignedByUserId;

    @NotNull(message ="assignee ID (assignToUserId) is required")
    @Schema(description = "UUID of the agent who will receive the ticket")
    private UUID assignedToUserId;
}
