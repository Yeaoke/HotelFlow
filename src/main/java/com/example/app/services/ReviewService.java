package com.example.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.app.dto.review.input.ReviewRequest;
import com.example.app.exceptions.ReservationNotFoundException;
import com.example.app.exceptions.ReviewNotFoundException;
import com.example.app.models.main.Reservation;
import com.example.app.models.main.Review;
import com.example.app.repos.main.ReservationRepository;
import com.example.app.repos.main.ReviewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public Review createReview(
            ReviewRequest dto,
            UUID reservationId
    ) {
        log.info("Creating review: reservationId={}",reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                        .orElseThrow(() -> new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        validateRating(dto.rating());

        Review review = new Review();

        review.setReservation(reservation);
        review.setRating(dto.rating());

        Review savedReview = reviewRepository.save(review);

        log.info("Review created: id={}, reservationId={}",savedReview.getId(), reservationId);

        return savedReview;
    }

    public Optional<Review> getReviewById(UUID id) {

        log.info(
                "Getting review: id={}",
                id
        );

        return reviewRepository.findById(id);
    }

    public List<Review> getAllReviews() {

        log.info("Getting all reviews");

        return reviewRepository.findAll();
    }

    @Transactional
    public void deleteReview(UUID id) {

        log.info(
                "Deleting review: id={}",
                id
        );

        Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new ReviewNotFoundException(
                                        "Review not found with id: " + id
                                )
                        );

        reviewRepository.delete(review);
    }

    private void validateRating(Integer rating) {

        if (rating == null) {
            throw new IllegalArgumentException(
                    "Rating can't be null"
            );
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );
        }
    }
}