package com.example.app.dto.user.output;

import java.util.UUID;

public record UserInfoResponse(
    UUID userId,

    String username,

    String email,

    String name,

    String lastname,

    String phoneNumber
) {}
