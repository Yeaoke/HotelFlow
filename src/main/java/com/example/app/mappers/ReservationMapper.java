package com.example.app.mappers;
import com.example.app.models.main.Reservation;
import com.example.app.dto.reservation.output.ReservationResponse;

public final class ReservationMapper {

    private ReservationMapper() {}

    public static ReservationResponse toDto(
        Reservation r
    ) {
        return new ReservationResponse(
            r.getId(),
            r.getUserId(),
            r.getRoomId(),
            r.getStartDate(),
            r.getEndDate(),
            r.getPrice(),
            r.getStatus());
    }
}
