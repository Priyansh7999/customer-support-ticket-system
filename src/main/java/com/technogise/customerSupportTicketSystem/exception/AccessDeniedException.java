package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class AccessDeniedException extends RuntimeException {
    private String code;
    public AccessDeniedException(String code,String message) {
        super(message);
        this.code = code;
    }
}


