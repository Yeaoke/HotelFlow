package com.example.app.controllers;

import com.example.app.dto.ReservationStatus;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.dto.reservation.output.ReservationResponse;
import com.example.app.models.main.Reservation;
import com.example.app.models.main.Review;
import com.example.app.services.ReservationService;
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
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
@SecurityRequirement(name = "Bearer Authentication")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReviewService reviewService;

    @Operation(
            summary = "Получить список бронирований",
            description = "Возвращает список бронирований текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бронирования успешно получены"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @GetMapping
    public List<ReservationResponse> getAllReservations(
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        return reservationService.getAllReservations(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Operation(
            summary = "Получить бронирование по ID",
            description = "Возвращает бронирование с указанным идентификатором"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бронирование успешно получено"
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
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        return reservationService.findReservation(id, userId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Создать бронирование",
            description = "Создаёт бронирование от имени текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Бронирование успешно создано"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные бронирования"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь или номер не найдены"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Номер уже забронирован"
            )
    })
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            Authentication authentication,
            @RequestBody @Valid ReservationRequest dto
    ) {
        UUID userId = getUserId(authentication);

        Reservation reservation =
                reservationService.createReservation(dto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDto(reservation));
    }

    @Operation(
            summary = "Обновить бронирование",
            description = "Обновляет бронирование текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бронирование успешно обновлено"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные бронирования"
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
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestBody @Valid ReservationRequest dto
    ) {
        UUID userId = getUserId(authentication);

        Reservation reservation =
                reservationService.updateReservation(id, dto, userId);

        return ResponseEntity.ok(toDto(reservation));
    }

    @Operation(
            summary = "Обновить статус бронирования",
            description = "Обновляет статус бронирования текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус бронирования успешно обновлён"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный статус бронирования"
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
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable UUID id,
            @RequestParam ReservationStatus status,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Reservation reservation =
                reservationService.updateReservationStatus(
                        id,
                        status,
                        userId
                );

        return ResponseEntity.ok(toDto(reservation));
    }

    @Operation(
            summary = "Получить отзыв о бронировании",
            description = "Возвращает отзыв, связанный с указанным бронированием"
    )
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
                    responseCode = "403",
                    description = "Нет доступа к данному бронированию"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование или отзыв не найдены"
            )
    })
    @GetMapping("/{reservationId}/reviews/{reviewId}")
    public ResponseEntity<Review> getReviewOfReservation(
            @PathVariable UUID reservationId,
            @PathVariable UUID reviewId
    ) {
        Optional<Review> review =
                reviewService.getReviewById(reviewId);

        if (review.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!review.get()
                .getReservation()
                .getId()
                .equals(reservationId)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(review.get());
    }

    @Operation(
            summary = "Удалить бронирование",
            description = "Удаляет бронирование текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Бронирование успешно удалено"
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
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        reservationService.deleteReservation(id, userId);

        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private ReservationResponse toDto(Reservation reservation) {
        return new ReservationResponse(
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getPrice(),
                reservation.getStatus()
        );
    }
}