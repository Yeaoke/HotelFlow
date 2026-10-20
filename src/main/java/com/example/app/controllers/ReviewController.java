package com.example.app.controllers;
import com.example.app.dto.review.input.ReviewRequest;
import com.example.app.dto.review.output.ReviewResponse;
import com.example.app.services.ReviewService;
import com.example.app.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.*;
@RestController @RequestMapping("/api/reservations/{reservationId}/reviews") @RequiredArgsConstructor
@SecurityRequirement(name="Bearer Authentication")
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(
        @PathVariable UUID reservationId,
        @Valid @RequestBody ReviewRequest dto,
        Authentication auth
    ) {
        var result = service.createReview(dto,reservationId,CurrentUser.id(auth));

        return ResponseEntity.created(java.net.URI.create("/api/reservations/"+reservationId+"/reviews/"+result.id())).body(result);
    }
    
    @GetMapping("/{reviewId}")
    public ReviewResponse get(
        @PathVariable UUID reservationId,
        @PathVariable UUID reviewId,
        Authentication auth
    ) {
        return service.getReviewById(reservationId,reviewId,CurrentUser.id(auth));
    }
    @GetMapping public List<ReviewResponse> all(
        @PathVariable UUID reservationId,
        Authentication auth
    ) {
        return service.getAllReviews(reservationId,CurrentUser.id(auth));
    }

    @DeleteMapping("/{reviewId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable UUID reservationId,
        @PathVariable UUID reviewId,
        Authentication auth
    ) {
        service.deleteReview(reservationId,reviewId,CurrentUser.id(auth));
    }
}
