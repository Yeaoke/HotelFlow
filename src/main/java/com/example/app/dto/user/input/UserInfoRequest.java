package com.example.app.dto.user.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserInfoRequest(

    @NotNull(message = "Firstname can't be empty")
    @Size(min = 2, max = 100)
    String firstname,

    @NotNull(message = "Lastname can't be empty")
    @Size(min = 2, max = 100)
    String lastname,

    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Number must be +*"
    )
    @NotNull
    String phoneNumber
) {}
