package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.RegisterUserRequest;
import com.technogise.customerSupportTicketSystem.dto.RegisterUserResponse;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.ConflictException;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository =userRepository;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional
    public RegisterUserResponse registerUser(RegisterUserRequest request) {

        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isPresent()) {
            throw new ConflictException("CONFLICT", "User with email:"+request.getEmail()+" already exists");
        }
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setRole(UserRole.CUSTOMER);
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        newUser.setPassword(hashedPassword);

        User savedUser = userRepository.save(newUser);
        return new RegisterUserResponse(
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getId()
        );
    }

}
