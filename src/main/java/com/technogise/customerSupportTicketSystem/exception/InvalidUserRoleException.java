package com.technogise.customerSupportTicketSystem.exception;

import lombok.Getter;

@Getter
public class InvalidUserRoleException extends RuntimeException {
<<<<<<< HEAD
    private String code;

=======
    private final String code;
>>>>>>> 70208a9 (refactor: rename the AgentRoleNotFoundException to InvalidUserRoleException)
    public InvalidUserRoleException(String code, String message) {
        super(message);
        this.code = code;
    }
}
