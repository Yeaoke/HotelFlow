package com.example.app.dto.auth.login.output;

public record RegisterResponse(
    String message,

    String username,

    String OTPCode
) {}
