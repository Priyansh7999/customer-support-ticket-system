package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository =userRepository;
    }

    public User getUserByIdAndRole(UUID id, UserRole role) {
        return userRepository.findByIdAndRole(id, role)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "INVALID_USER_ID", "User not found with id: " + id + " and role: " + role)
                );
    }

    public User getRandomUserByRole(UserRole role) {
        return userRepository.findFirstByRole(role)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NO_USER_FOUND", "No user found with role: " + role)
                );
    }
}
