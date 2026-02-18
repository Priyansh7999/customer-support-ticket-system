package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class InvalidUserRoleException extends RuntimeException {
    private final String code;
    public InvalidUserRoleException(String code, String message) {
        super(message);
        this.code = code;
    }
}
