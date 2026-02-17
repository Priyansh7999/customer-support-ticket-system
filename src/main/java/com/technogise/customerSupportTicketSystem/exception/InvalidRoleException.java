package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class InvalidRoleException extends RuntimeException {

    private final String code;

    public InvalidRoleException(String code, String message) {
        super(message);
        this.code = code;
    }
}
