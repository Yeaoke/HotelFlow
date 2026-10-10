
package com.example.app.dto.user.output;

import java.util.UUID;

public record UserInfoResponse(
    UUID id,
    String firstname,
    String lastname,
    String username,
    String email,
    String phoneNumber
) {}
