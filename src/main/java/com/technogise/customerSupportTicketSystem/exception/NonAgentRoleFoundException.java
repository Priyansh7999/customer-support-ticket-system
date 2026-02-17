package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class NonAgentRoleFoundException extends RuntimeException {
    private final String code;
    public NonAgentRoleFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
