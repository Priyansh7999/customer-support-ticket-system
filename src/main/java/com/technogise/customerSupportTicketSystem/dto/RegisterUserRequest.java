package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {

    @NotBlank(message ="Name is required")
    @Size(max = 50,message = "Name must node exceed 50 characters")
    @Pattern(regexp = "^(?!\\d+$).+", message = "name must be a string and should contain letters")
    @Schema(description = "Name of the user", example = "Jatin Joshi")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Email address", example = "jatin@technogise.com")
    private String email;


    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters long")
    @Schema(description = "Password (min 8 chars, 1 special, 1 uppercase, 1 number)", example = "Password@123")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must include at least one uppercase, one lowercase, one number, and one special character"
    )
    private String password;
}
