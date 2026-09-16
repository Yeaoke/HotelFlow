package com.example.app.dto.auth.login.output;

import lombok.Getter;

@Getter
public class AuthResponse {
    
    final String accessToken;

    final String refreshToken;

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
