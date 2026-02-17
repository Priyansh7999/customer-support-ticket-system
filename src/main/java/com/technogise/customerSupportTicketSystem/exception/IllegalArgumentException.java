package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class IllegalArgumentException extends RuntimeException {
    private final String code;
    public IllegalArgumentException(String code, String message) {
        super(message);
        this.code = code;
    }
}
