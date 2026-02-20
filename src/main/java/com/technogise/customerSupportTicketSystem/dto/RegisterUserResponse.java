package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserResponse {
    private String name;
    private String email;
    private UserRole role;
    private UUID userId;
    private String message;
}
