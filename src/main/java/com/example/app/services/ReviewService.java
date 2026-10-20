package com.example.app.services;
import com.example.app.dto.review.input.ReviewRequest;
import com.example.app.dto.review.output.ReviewResponse;
import com.example.app.exceptions.*;
import com.example.app.models.main.*;
import com.example.app.repos.main.*;
import com.example.app.mappers.ReviewMapper;
import com.example.app.policies.ReservationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
@Service @RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviews;
    private final ReservationRepository reservations;

    @Transactional
    public ReviewResponse createReview(
        ReviewRequest dto,
        UUID reservationId,
        UUID userId
    ) {
        Reservation r = owned(reservationId,userId,true);
        
        ReservationPolicy.requireReviewable(r);
        
        if (dto == null || dto.rating() == null || dto.rating() < 1 || dto.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        if (reviews.existsByReservationId(reservationId)) {
            throw new ConflictException("Reservation already has a review");
        }
        
        Review review = new Review();

        review.setReservation(r);

        review.setRating(dto.rating());

        return ReviewMapper.toDto(reviews.saveAndFlush(review));
    }
    @Transactional(readOnly=true)
    public ReviewResponse getReviewById(
        UUID reservationId,
        UUID reviewId,
        UUID userId
    ) {
        owned(reservationId,userId,false);

        return ReviewMapper.toDto(reviews.findByIdAndReservationId(reviewId,reservationId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found for this reservation")));
    }
    @Transactional(readOnly=true)
    public List<ReviewResponse> getAllReviews(
        UUID reservationId,
        UUID userId
    ) {
        owned(reservationId,userId,false);

        return reviews.findAllByReservationId(reservationId).stream().map(ReviewMapper::toDto).toList();
    }
    @Transactional
    public void deleteReview(
        UUID reservationId,
        UUID reviewId,
        UUID userId
    ) {
        owned(reservationId,userId,true);

        Review review = reviews.findByIdAndReservationId(reviewId,reservationId)
                    .orElseThrow(()->new ReviewNotFoundException("Review not found for this reservation"));

        reviews.delete(review);
    }
    private Reservation owned(
        UUID id,
        UUID userId,
        boolean lock
    ) {
        Reservation r = (lock?reservations.findByIdForUpdate(id):reservations.findById(id))
                    .orElseThrow(()->new ReservationNotFoundException("Reservation not found"));

        if (!r.getUserId().equals(userId)) {
            throw new AccessDeniedException("Reservation belongs to another user");
        }
        return r;
    }
}
