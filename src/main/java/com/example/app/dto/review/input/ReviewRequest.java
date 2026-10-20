package com.example.app.dto.review.input;
import jakarta.validation.constraints.*;
public record ReviewRequest(
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating
) {}
