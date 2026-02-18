package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.constant.Constants;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.dto.CreateUserRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateUserResponse;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.response.SuccessResponse;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import com.technogise.customerSupportTicketSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping
    public ResponseEntity<SuccessResponse<CreateUserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request){
        String name=request.getName();
        String email=request.getEmail();
        UserRole role=request.getRole();
        CreateUserResponse createdUserResponse=userService.createUser(name,role,email);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.success("User created successfully", createdUserResponse)
        );
    }
}
