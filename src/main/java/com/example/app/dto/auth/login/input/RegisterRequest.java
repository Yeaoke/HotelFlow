package com.example.app.dto.auth.login.input;
import jakarta.validation.constraints.*;
public record RegisterRequest(
    @NotBlank
    @Pattern(regexp="[A-Za-z0-9_.-]{3,64}")
    String username,

    @NotBlank
    @Email
    @Size(max=254)
    String email,

    @NotBlank
    @Size(min=8,max=72)
    String password
) {}
