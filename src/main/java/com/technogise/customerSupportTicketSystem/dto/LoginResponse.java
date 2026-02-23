package com.technogise.customerSupportTicketSystem.dto;

import lombok.*;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UUID userId;
    private String email;
    private String role;
}