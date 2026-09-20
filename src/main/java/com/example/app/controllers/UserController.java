package com.example.app.controllers;

import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.input.RegisterRequest;
import com.example.app.dto.auth.login.output.LoginResponse;
import com.example.app.dto.auth.login.output.RegisterResponse;
import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.UserInfoResponse;
import com.example.app.models.User;
import com.example.app.services.AuthencationService;
import com.example.app.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;






@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    private final AuthencationService authencationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
        @RequestBody @Valid RegisterRequest registerRequest
    ) {
        authencationService.register(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @RequestBody @Valid LoginRequest loginRequest
        
    ) {
        authencationService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(
        Authentication authentication,
        @RequestBody UserInfoResponse response
    ) {
        User user = (User) authentication.getPrincipal();
    
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }
    

    @PutMapping("/me/update")
    public ResponseEntity<Void> updateUserProfile(
        Authentication authentication,
        @RequestBody UserInfoRequest request
    ) {
        User user = (User) authentication.getPrincipal();

        userService.updateUserDetails(user.getId(), request);

        return ResponseEntity.ok().build();
    }
}