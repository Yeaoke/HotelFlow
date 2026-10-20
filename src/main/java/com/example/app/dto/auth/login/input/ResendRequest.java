package com.example.app.dto.auth.login.input;
import jakarta.validation.constraints.*;
public record ResendRequest(
    @NotBlank
    @Size(max=64)
    String username
) {}
