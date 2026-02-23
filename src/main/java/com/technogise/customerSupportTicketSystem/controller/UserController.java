package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.LoginRequest;
import com.technogise.customerSupportTicketSystem.dto.LoginResponse;
import com.technogise.customerSupportTicketSystem.dto.RegisterUserRequest;
import com.technogise.customerSupportTicketSystem.dto.RegisterUserResponse;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User registrationn", description = "Endpoints for user registration")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    @Operation(summary = "Register a new account", description = "Creates a new user with the CUSTOMER role.")
    public ResponseEntity<SuccessResponse<RegisterUserResponse>> registerUser(
            @Valid @RequestBody RegisterUserRequest request){
        RegisterUserResponse createdUserResponse=userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.success("User created successfully", createdUserResponse)
        );
    }
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.success("Login successful",response)
        );
    }
}
