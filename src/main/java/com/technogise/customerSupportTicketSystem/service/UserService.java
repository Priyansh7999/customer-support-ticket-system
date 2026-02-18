package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.ConflictException;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository =userRepository;
    }

    public User getUserByIdAndRole(UUID id, UserRole role) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "INVALID_USER_ID", "User not found with id: " + id)
                );

        if (user.getRole() != role) {
            throw new InvalidUserRoleException(
                    "FORBIDDEN",
                    "User is not authorized to perform this action. Required role: " + role
                    );
        }

        return user;
    }

    public User getRandomUserByRole(UserRole role) {
        return userRepository.findFirstByRole(role)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NO_USER_FOUND", "No user found with role: " + role)
                );
    }
    public CreateUserResponse createUser(String name, UserRole role, String email) {
        email = email.trim().toLowerCase();
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            throw new ConflictException("409","User already exists with given email");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email);
        user.setRole(role);

        return new CreateUserResponse(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getId()
        );
    }

}
