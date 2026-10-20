package com.example.app.dto.reservation.output;
import java.util.UUID;
import java.time.LocalDate;
import com.example.app.dto.ReservationStatus;
public record ReservationResponse(
    UUID id,
    UUID userId,
    UUID roomId,
    LocalDate startDate,
    LocalDate endDate,
    Long price,
    ReservationStatus status
) {}
