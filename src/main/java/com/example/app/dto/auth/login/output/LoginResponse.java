package com.example.app.dto.auth.login.output;

public record LoginResponse(
        String accessToken,

        String refreshToken
) {}