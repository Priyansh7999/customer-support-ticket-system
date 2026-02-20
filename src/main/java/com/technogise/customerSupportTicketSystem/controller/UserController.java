package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.RegisterUserRequest;
import com.technogise.customerSupportTicketSystem.dto.RegisterUserResponse;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<RegisterUserResponse>> registerUser(
            @Valid @RequestBody RegisterUserRequest request){
        RegisterUserResponse createdUserResponse=userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.success("User created successfully", createdUserResponse)
        );
    }
}
