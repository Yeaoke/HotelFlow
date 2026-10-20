package com.example.app.dto.auth.login.input;
import jakarta.validation.constraints.*;
public record VerifyRequest(
    @NotBlank @Size(max=64) String username,

    @NotBlank
    @Pattern(regexp="[0-9]{6}")
    String OTPCode
) {}
