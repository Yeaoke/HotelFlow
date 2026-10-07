package com.example.app.dto.user.input;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserInfoRequest(

    @Size(min = 2, max = 100)
    String firstname,

    @Size(min = 2, max = 100)
    String lastname,

    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Phone number must be in normal format"
    )
    String phoneNumber
) {}