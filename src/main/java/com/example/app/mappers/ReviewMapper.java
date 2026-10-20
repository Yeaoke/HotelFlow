package com.example.app.mappers;
import com.example.app.models.main.Review;
import com.example.app.dto.review.output.ReviewResponse;
public final class ReviewMapper {

    private ReviewMapper() {}

    public static ReviewResponse toDto(
        Review r
    ) {
        return new ReviewResponse(
            r.getId(),
            r.getReservation().getId(),
            r.getRating());
    }
}
