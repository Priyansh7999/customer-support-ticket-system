package com.technogise.customerSupportTicketSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Pattern(regexp = "^(?!\\d+$).+", message = "Title must be a string and should contain letters")
    @Schema(description = "Title of the issue", example = "Issue related to login into the account")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Detailed explanation of the problem", example = "I am receiving a 400 error every time I try to login.")
    @Pattern(regexp = "^(?!\\d+$).+", message = "Description must be a string and should contain letters")
    private String description;
}
