package com.technogise.customerSupportTicketSystem.utils;

import com.technogise.customerSupportTicketSystem.enums.UserRole;

import java.util.List;
import java.util.Map;

public class UserPermissions {
    private static final Map<UserRole, List<String>> rolePermissions = Map.of(
            UserRole.CUSTOMER, List.of(
                    "CREATE_TICKET",
                    "VIEW_OWN_TICKETS",
                    "UPDATE_TICKET_DESCRIPTION",
                    "CLOSE_TICKET"
            ),
            UserRole.SUPPORT_AGENT, List.of(
                    "VIEW_ASSIGNED_TICKETS",
                    "UPDATE_TICKET_STATUS",
                    "UPDATE_TICKET_PRIORITY",
                    "REASSIGN_TICKET"
            )
    );

    public static List<String> getPermissions(UserRole role) {
        return rolePermissions.getOrDefault(role, List.of());
    }
}
