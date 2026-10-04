package com.example.app.dto.auth.login.input;

public record VerifyOTPRequest(
    String username,

    String OTPCode
) {}
