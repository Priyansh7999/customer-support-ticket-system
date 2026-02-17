package com.technogise.customerSupportTicketSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequest {
    @NotBlank(message = "Comment cannot be blank or null")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String body;
}
