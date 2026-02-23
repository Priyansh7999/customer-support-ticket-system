package com.technogise.customerSupportTicketSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "Invalid email")
    @Schema(description = "Registered email address", example = "jatin@technogise.com")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "User password", example = "Password@123")
    @NotBlank(message = "Password is required")
    private String password;
}