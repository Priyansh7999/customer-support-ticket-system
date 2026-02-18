package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message ="name is required")
    @Size(max = 50,message = "name must node exceed 50 characters")
    @Pattern(regexp = "^(?!\\d+$).+", message = "name must be a string and should contain letters")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;


    @NotNull(message = "role is required")
    private UserRole role;

}
