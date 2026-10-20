package com.example.app.controllers;
import com.example.app.dto.auth.login.input.*;
import com.example.app.dto.auth.login.output.*;
import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.*;
import com.example.app.services.*;
import com.example.app.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class UserController {

    private final UserService users;
    private final AuthenticationService auth;
    private final AuthRateLimiter limits;

    @Operation(summary="Register and send an email verification code")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest http
    ) {
        limits.check("register",http.getRemoteAddr());
        
        return auth.register(request,http.getRemoteAddr());
    }

    @PostMapping("/register/verify")
    public VerifyResponse verify(
        @Valid @RequestBody VerifyRequest request,
        HttpServletRequest http
    ) {
        limits.check("verify",http.getRemoteAddr()); return auth.verifyUser(request);
    }

    @PostMapping("/register/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resend(
        @Valid @RequestBody ResendRequest request,
        HttpServletRequest http
    ) {
        limits.check("resend", http.getRemoteAddr());

        auth.resend(request,http.getRemoteAddr());
    }

    @PostMapping("/login")
    public LoginResponse login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest http
    ) {
        limits.check("login", http.getRemoteAddr());

        return auth.login(request);
    }

    @Operation(summary="Rotate a refresh token; the previous token pair becomes invalid")
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request,HttpServletRequest http) {
        limits.check("refresh",http.getRemoteAddr()); return auth.refresh(request.refreshToken());
    }

    @GetMapping("/me")
    @SecurityRequirement(name="Bearer Authentication")
    public UserInfoResponse me(Authentication authentication) {
        return users.getUserProfile(CurrentUser.id(authentication));
    }

    @PutMapping("/me")
    @SecurityRequirement(name="Bearer Authentication")
    public UserUpdateResponse update(
        Authentication authentication,
        @Valid @RequestBody UserInfoRequest request
    ) {
        return users.updateUserDetails(CurrentUser.id(authentication), request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name="Bearer Authentication")
    public void logout(
        Authentication authentication,
        @RequestHeader("Authorization") String header
    ) {
        auth.logout(header.substring(7).trim(), CurrentUser.id(authentication));

        SecurityContextHolder.clearContext();
    }
}
