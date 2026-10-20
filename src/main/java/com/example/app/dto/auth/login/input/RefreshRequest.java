package com.example.app.dto.auth.login.input;
import jakarta.validation.constraints.*;
public record RefreshRequest(
    @NotBlank @Size(max=4096) String refreshToken
) {}
