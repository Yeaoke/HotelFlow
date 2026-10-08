package com.example.app.testControllers;

import com.example.app.controllers.ReviewController;
import com.example.app.dto.review.input.ReviewRequest;
import com.example.app.models.main.Review;
import com.example.app.services.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID reservationId;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();

        userId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
    }

    @Test
    void createReview_true() throws Exception {

        when(authentication.getName()).thenReturn(userId.toString());

        ReviewRequest request = new ReviewRequest(reservationId, 8);

        Review review = new Review();
        review.setId(reviewId);
        review.setRating(8);

        when(reviewService.createReview(
                any(ReviewRequest.class),
                eq(reservationId),
                eq(userId)
        )).thenReturn(review);

        mockMvc.perform(
                post("/api/reservations/{reservationId}/reviews", reservationId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(review)
                ));

        verify(reviewService).createReview(
                any(ReviewRequest.class),
                eq(reservationId),
                eq(userId)
        );
    }

    @Test
    void getReviewById_true() throws Exception {

        Review review = new Review();
        review.setId(reviewId);
        review.setRating(8);

        when(reviewService.getReviewById(reviewId))
                .thenReturn(Optional.of(review));

        mockMvc.perform(
                get("/api/reservations/{reservationId}/reviews/{reviewId}",
                        reservationId, reviewId)
        )
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(review)
                ));

        verify(reviewService).getReviewById(reviewId);
    }

    @Test
    void getReviewById_notFound() throws Exception {

        when(reviewService.getReviewById(reviewId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                get("/api/reservations/{reservationId}/reviews/{reviewId}",
                        reservationId, reviewId)
        )
                .andExpect(status().isNotFound());

        verify(reviewService).getReviewById(reviewId);
    }

    @Test
    void getAllReviews_true() throws Exception {

        Review review1 = new Review();
        review1.setId(UUID.randomUUID());
        review1.setRating(8);

        Review review2 = new Review();
        review2.setId(UUID.randomUUID());
        review2.setRating(10);

        when(reviewService.getAllReviews())
                .thenReturn(List.of(review1, review2));

        mockMvc.perform(
                get("/api/reservations/{reservationId}/reviews", reservationId)
        )
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(
                                List.of(review1, review2)
                        )
                ));

        verify(reviewService).getAllReviews();
    }

    @Test
    void deleteReview_true() throws Exception {

        when(authentication.getName()).thenReturn(userId.toString());

        doNothing().when(reviewService)
                .deleteReview(reviewId, userId);

        mockMvc.perform(
                delete("/api/reservations/{reservationId}/reviews/{reviewId}",
                        reservationId, reviewId)
                        .principal(authentication)
        )
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(reviewId, userId);
    }
}