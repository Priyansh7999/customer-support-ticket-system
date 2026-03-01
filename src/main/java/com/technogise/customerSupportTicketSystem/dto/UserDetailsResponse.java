package com.technogise.customerSupportTicketSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserDetailsResponse {
    private UUID userId;
    private String email;
    private String role;
    public UserDetailsResponse(UUID userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}
