package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class ClosedTicketStatusException extends RuntimeException {
    private final String code;
    public ClosedTicketStatusException(String code, String message) {
        super(message);
        this.code = code;
    }
}
