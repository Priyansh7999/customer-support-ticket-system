package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class InvalidTicketStatusChangeException extends RuntimeException {
    private final String code;

    public InvalidTicketStatusChangeException(String code, String message) {
        super(message);
        this.code = code;
    }
}