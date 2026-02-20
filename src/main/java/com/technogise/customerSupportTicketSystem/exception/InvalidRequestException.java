package com.technogise.customerSupportTicketSystem.exception;
import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final String code;

    public InvalidRequestException(String code, String message) {
        super(message);
        this.code=code;
    }
}
