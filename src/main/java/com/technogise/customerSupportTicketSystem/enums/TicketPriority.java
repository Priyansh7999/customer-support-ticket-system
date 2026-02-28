package com.technogise.customerSupportTicketSystem.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.technogise.customerSupportTicketSystem.exception.BadRequestException;

public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH;

    @JsonCreator
    public static TicketPriority from(String value) {
        try {
            return TicketPriority.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("INVALID_ENUM_VALUE", "Invalid priority value: " + value);
        }
    }

}
