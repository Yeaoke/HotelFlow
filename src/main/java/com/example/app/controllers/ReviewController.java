package com.example.app.controllers;

import com.example.app.dto.review.input.ReviewRequest;
import com.example.app.models.main.Review;
import com.example.app.services.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations/{reservationId}/reviews")
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
        summary = "Создать отзыв",
        description = "Создаёт отзыв к бронированию текущего авторизованного пользователя")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Отзыв успешно создан"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Некорректные данные отзыва"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Пользователь не авторизован"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Нет доступа к данному бронированию"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Бронирование не найдено"
        )
    })
    @PostMapping
    public ResponseEntity<Review> createReview(
            @PathVariable UUID reservationId,
            Authentication authentication,
            @RequestBody @Valid ReviewRequest dto
    ) {
        UUID userId = getUserId(authentication);
        Review createdReview = reviewService.createReview(dto, reservationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @Operation(
        summary = "Получить отзыв",
        description = "Возвращает отзыв по указанному идентификатору")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Отзыв успешно получен"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Пользователь не авторизован"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Отзыв не найден"
        )
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReviewById(
            @PathVariable UUID reservationId,
            @PathVariable UUID reviewId
    ) {
        return reviewService.getReviewById(reviewId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Получить список отзывов",
        description = "Возвращает список всех отзывов")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Отзывы успешно получены"
        ),
        @ApiResponse(
            responseCode = "401",  
            description = "Пользователь не авторизован"
        )
    })
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(
            @PathVariable UUID reservationId
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @Operation(
        summary = "Удалить отзыв",
        description = "Удаляет отзыв текущего авторизованного пользователя")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204", 
            description = "Отзыв успешно удалён"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Пользователь не авторизован"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Нет доступа к данному отзыву"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Отзыв не найден"
        )
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reservationId,
            @PathVariable UUID reviewId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}