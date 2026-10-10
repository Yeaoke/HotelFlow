package com.example.app.dto.auth.login.input;

public record VerifyRequest(
    String username,

    String OTPCode
) {}
