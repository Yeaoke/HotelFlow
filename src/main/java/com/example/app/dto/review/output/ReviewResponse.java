package com.example.app.dto.review.output;
import java.util.UUID;
public record ReviewResponse(
    UUID id,
    UUID reservationId,
    Integer rating
) {}
