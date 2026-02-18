package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
    }

    @Test
    void shouldReturnUser_WhenUserExistsAndRoleMatches() {
        // Given
        testUser.setRole(UserRole.CUSTOMER);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        User user = userService.getUserByIdAndRole(testUser.getId(), UserRole.CUSTOMER);

        // Then
        assertEquals(testUser, user);
        assertEquals(testUser.getId(), user.getId());
        assertEquals(testUser.getRole(), user.getRole());
    }

    @Test
    void shouldThrowResourceNotFoundException_WhenUserNotFoundOrRoleDoesNotMatch() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByIdAndRole(userId, UserRole.CUSTOMER));

        assertEquals(
                "INVALID_USER_ID",
                exception.getCode()
        );
        assertEquals(
                "User not found with id: " + userId,
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnRandomUser_WhenUserWithRoleExists() {
        // Given
        testUser.setRole(UserRole.SUPPORT_AGENT);
        when(userRepository.findFirstByRole(UserRole.SUPPORT_AGENT)).thenReturn(Optional.of(testUser));

        // When
        User user = userService.getRandomUserByRole(UserRole.SUPPORT_AGENT);

        // Then
        assertEquals(testUser, user);
        assertEquals(testUser.getRole(), user.getRole());
    }

    @Test
    void shouldThrowResourceNotFoundException_WhenNoUserWithRoleExists() {
        // Given
        when(userRepository.findFirstByRole(UserRole.SUPPORT_AGENT)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getRandomUserByRole(UserRole.SUPPORT_AGENT));

        assertEquals(
                "NO_USER_FOUND",
                exception.getCode()
        );
        assertEquals(
                "No user found with role: " + UserRole.SUPPORT_AGENT,
                exception.getMessage()
        );
    }
}
