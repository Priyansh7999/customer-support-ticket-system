package com.technogise.customerSupportTicketSystem.response;

import lombok.*;

@Getter
@Setter
public class ErrorResponse extends RuntimeException {
    private String code;

    public ErrorResponse(String code, String message) {
        super(message);
        this.code = code;
    }
}
