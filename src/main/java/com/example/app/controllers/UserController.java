package com.example.app.controllers;

import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.input.RegisterRequest;
import com.example.app.dto.auth.login.input.VerifyOTPRequest;
import com.example.app.dto.auth.login.output.LoginResponse;
import com.example.app.dto.auth.login.output.RegisterResponse;
import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.UserInfoResponse;
import com.example.app.models.main.User;
import com.example.app.services.AuthenticationService;
import com.example.app.services.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    private final AuthenticationService authenticationService;

    public UserController(
        UserService userService, 
        AuthenticationService authencationService
    ) {
        this.userService = userService;
        this.authenticationService = authencationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<Void> verify(
            @RequestBody VerifyOTPRequest request
    ) {
        authenticationService.verifyUser(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authenticationService.login(request));
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