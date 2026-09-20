package com.example.app.dto.auth.login.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotNull(message = "username cann't be null")
    String username,

    @Email(message = "input correct email")
    @NotNull
    String email,

    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Пароль должен содержать: минимум 8 символов, цифру, заглавную и строчную букву, спецсимвол (@#$%^&+=!)"
    )
    @NotBlank
    @Size(min = 8)
    String password
) {}
