package com.example.app.dto.user.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserInfoRequest(

    @NotNull(message = "Name can't be empty")
    @Size(min = 2, max = 100)
    String name,

    @Email(message = "input correct email")
    @NotNull
    String email,

    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Number must be +*"
    )
    @NotNull
    String phoneNumber
) {}
