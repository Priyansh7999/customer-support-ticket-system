package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class TicketNotFoundException extends RuntimeException {
    private final String code;

    public TicketNotFoundException(String code, String message) {
        this.code = code;
        super(message);
    }
}
