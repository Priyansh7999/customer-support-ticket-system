package com.technogise.customerSupportTicketSystem.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.technogise.customerSupportTicketSystem.exception.BadRequestException;

public enum TicketStatus {
    OPEN{
        @Override
        public boolean canTransitionTo(TicketStatus next) {
            return next == IN_PROGRESS || next == CLOSED;
        }
    },
    IN_PROGRESS{
        @Override
        public boolean canTransitionTo(TicketStatus next) {
            return next == CLOSED;
        }
    },
    CLOSED{
        @Override
        public boolean canTransitionTo(TicketStatus next) {
            return false;
        }
    };
    public abstract boolean canTransitionTo(TicketStatus next);

    @JsonCreator
    public static TicketStatus from(String value) {
        try {
            return TicketStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("INVALID_ENUM_VALUE", "Invalid status value: " + value);
        }
    }

}
