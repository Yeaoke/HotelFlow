package com.example.app.dto.auth.login.output;

import lombok.Getter;

@Getter
public class LoginResponse {
    
    final String accessToken;

    final String refreshToken;

    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
