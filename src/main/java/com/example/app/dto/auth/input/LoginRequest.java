package com.example.app.dto.auth.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotNull(message = "username cann't be null")
    String username,

    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Пароль должен содержать: минимум 8 символов, цифру, заглавную и строчную букву, спецсимвол (@#$%^&+=!)"
    )
    @NotBlank
    @Size(min = 8)
    String password
) {}
