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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReviewService reviewService;

    @Operation(
        summary = "Вернуть список всех бронирований",
        description = "Возвращает весь вообще список забронированных, которые есть в базе данных"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Бронирование создано"
    ),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь или номер не найден"
    ),
        @ApiResponse(
            responseCode = "409",
            description = "Номер уже забронирован"
    )
})
    @GetMapping("")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    
    @Operation(
        summary = "Вернуть ",
        description = ""
    )
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable UUID id
    ) {
        return reservationService.findReservation(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<ReservationResponse> createReservation(
            @PathVariable Authentication authentication,
            @RequestBody @Valid ReservationRequest dto
    ) {
        UUID userId = UUID.fromString(null);

        Reservation reservation = reservationService.createReservation(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(reservation));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable UUID id,
            @RequestBody @Valid ReservationRequest dto
    ) {
        Reservation reservation = reservationService.updateReservation(id, dto);
        return ResponseEntity.ok(toDto(reservation));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable UUID id,
            @RequestParam ReservationStatus status
    ) {
        return ResponseEntity.ok(toDto(
                reservationService.updateReservationStatus(id, status)
        ));
    }

    @GetMapping("/{reservationId}/reviews/{reviewId}")
    public ResponseEntity<Review> getReviewOfReservation(
            @PathVariable UUID reservationId,
            @PathVariable UUID reviewId
    ) {
        Optional<Review> review = reviewService.getReviewById(reviewId);

        if (review.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!review.get().getReservation().getId().equals(reservationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(review.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable UUID id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
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
