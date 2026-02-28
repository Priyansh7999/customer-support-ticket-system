package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class InvalidStateTransitionException extends RuntimeException {

    private final String code;

    public InvalidStateTransitionException(String code, String message) {
        super(message);
        this.code = code;
    }
}
