package com.technogise.customerSupportTicketSystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateCommentResponse {
    private UUID id;
    private String body;
    private LocalDateTime createdAt;
}
