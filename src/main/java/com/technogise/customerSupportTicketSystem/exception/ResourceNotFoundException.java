package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private String code;

    public ResourceNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
