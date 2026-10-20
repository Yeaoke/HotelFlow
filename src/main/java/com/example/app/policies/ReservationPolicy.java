package com.example.app.policies;
import com.example.app.dto.ReservationStatus;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.models.main.Reservation;
import com.example.app.exceptions.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
public final class ReservationPolicy {
    private ReservationPolicy() {}
    public static void validateDates(ReservationRequest d) {
        if (d==null || d.roomId()==null || d.start_date()==null || d.end_date()==null)
            throw new DaysAmountException("Room and dates are required");
        long nights=ChronoUnit.DAYS.between(d.start_date(),d.end_date());
        if (d.start_date().isBefore(LocalDate.now()) || nights<1 || nights>30)
            throw new DaysAmountException("Booking must start today or later and last 1 to 30 nights");
    }
    public static void requireEditable(Reservation r) {
        if ((r.getStatus()!=ReservationStatus.PENDING && r.getStatus()!=ReservationStatus.APPROVED)
                || r.getStartDate().isBefore(LocalDate.now()))
            throw new ConflictException("This reservation can no longer be changed");
    }
    public static void transition(Reservation r,ReservationStatus target) {
        if (target==null) throw new IllegalArgumentException("Status is required");
        if (r.getStatus()==target) return;
        if (target==ReservationStatus.COMPLETED && r.getStatus()==ReservationStatus.APPROVED
                && !r.getEndDate().isAfter(LocalDate.now())) return;
        requireEditable(r);
        if (target==ReservationStatus.CANCELED) return;
        if (r.getStatus()==ReservationStatus.PENDING && target==ReservationStatus.APPROVED) return;
        throw new ConflictException("Reservation status transition is not allowed");
    }
    public static void requireReviewable(Reservation r) {
        if ((r.getStatus()!=ReservationStatus.APPROVED && r.getStatus()!=ReservationStatus.COMPLETED)
                || r.getEndDate().isAfter(LocalDate.now()))
            throw new ConflictException("A review requires a completed stay");
    }
}
