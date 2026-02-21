package com.technogise.customerSupportTicketSystem.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.technogise.customerSupportTicketSystem.exception.BadRequestException;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    CLOSED;

      @JsonCreator
    public static TicketStatus from(String value) {
        try {
            return TicketStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("INVALID_ENUM_VALUE", "Invalid status value: " + value);
        }
    }

}
