package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class ResourceNotFound extends RuntimeException {
    private String code;

    public ResourceNotFound(String code, String message) {
        super(message);
        this.code = code;
    }
}
