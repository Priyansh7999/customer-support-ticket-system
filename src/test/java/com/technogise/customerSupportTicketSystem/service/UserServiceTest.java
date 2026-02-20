package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.RegisterUserRequest;
import com.technogise.customerSupportTicketSystem.dto.RegisterUserResponse;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.ConflictException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    void shouldThrowConflictException_WhenEmailAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest("Jatin", "jatin@gmail.com", "Password@123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("CONFLICT", exception.getCode());
        assertEquals("User with email:"+request.getEmail()+" already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldCreateUserSuccessfully_WhenUserDoesNotExist() {
        String rawPassword = "Password@123";
        String encodedPassword = "hashedPassword123";
        RegisterUserRequest request = new RegisterUserRequest("Jatin", "jatin@gmail.com", rawPassword);

        UUID generatedId = UUID.randomUUID();
        User savedUser = new User();
        savedUser.setId(generatedId);
        savedUser.setName(request.getName());
        savedUser.setEmail(request.getEmail());
        savedUser.setRole(UserRole.CUSTOMER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(savedUser);

        RegisterUserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(generatedId, response.getId());

        User capturedUser = userCaptor.getValue();
        assertEquals("Jatin", capturedUser.getName());
        assertEquals("jatin@gmail.com", capturedUser.getEmail());
        assertEquals(UserRole.CUSTOMER, capturedUser.getRole());

        assertEquals(encodedPassword, capturedUser.getPassword());
        assertNotEquals(rawPassword, capturedUser.getPassword());

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }
}
