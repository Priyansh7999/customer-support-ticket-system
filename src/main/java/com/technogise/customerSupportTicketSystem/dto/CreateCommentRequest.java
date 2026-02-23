package com.technogise.customerSupportTicketSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequest {
    @NotBlank(message = "Comment cannot be blank or null")
    @Schema(description = "The body/content of the comment", example = "I am looking into this issue, please provide your logs.")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String body;
}
