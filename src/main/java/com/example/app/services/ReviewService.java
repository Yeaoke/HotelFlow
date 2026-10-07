package com.example.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.dto.review.input.ReviewRequest;
import com.example.app.exceptions.ReservationNotFoundException;
import com.example.app.exceptions.ReviewNotFoundException;
import com.example.app.models.main.Reservation;
import com.example.app.models.main.Review;
import com.example.app.repos.main.ReservationRepository;
import com.example.app.repos.main.ReviewRepository;

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
            UUID reservationId,
            UUID userId
    ) {
        if (reservationId == null) {
            throw new ReservationNotFoundException(
                    "Reservation id can't be null"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id can't be null"
            );
        }

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Review data can't be null"
            );
        }

        log.info(
                "Creating review: reservationId={}, userId={}",
                reservationId,
                userId
        );

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(
                        () -> new ReservationNotFoundException("Reservation not found with id: " + reservationId)
                );

        checkOwner(reservation, userId);

        validateRating(dto.rating());

        Review review = new Review();

        review.setReservation(reservation);
        review.setRating(dto.rating());

        Review savedReview = reviewRepository.save(review);

        log.info(
                "Review created: id={}, reservationId={}, userId={}",
                savedReview.getId(),
                reservationId,
                userId
        );

        return savedReview;
    }

    @Transactional(readOnly = true)
    public Optional<Review> getReviewById(UUID id) {

        if (id == null) {
            return Optional.empty();
        }

        log.info(
                "Getting review: id={}",
                id
        );

        return reviewRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {

        log.info("Getting all reviews");

        return reviewRepository.findAll();
    }

    @Transactional
    public void deleteReview(
            UUID id,
            UUID userId
    ) {
        if (id == null) {
            throw new ReviewNotFoundException(
                    "Review id can't be null"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id can't be null"
            );
        }

        log.info(
                "Deleting review: id={}, userId={}",
                id,
                userId
        );

        Review review = reviewRepository.findById(id)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + id)
                );

        Reservation reservation = review.getReservation();

        if (reservation == null) {
            throw new ReservationNotFoundException(
                    "Reservation for review not found"
            );
        }

        checkOwner(reservation, userId);

        reviewRepository.delete(review);

        log.info(
                "Review deleted: id={}, userId={}",
                id,
                userId
        );
    }

    private void checkOwner(
            Reservation reservation,
            UUID userId
    ) {
        if (reservation.getUser() == null || reservation.getUser().getId() == null || !reservation.getUser().getId().equals(userId)) {

            throw new AccessDeniedException(
                    "You don't have access to this reservation"
            );
        }
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