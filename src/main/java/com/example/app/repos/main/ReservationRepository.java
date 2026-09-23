package com.example.app.repos.main;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.app.models.main.Reservation;
import com.example.app.models.main.Room;

import jakarta.persistence.LockModeType;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

        @Query("""
                SELECT COUNT(r) = 0
                FROM Reservation r
                WHERE r.room = :room
                AND r.startDate < :endDate
                AND r.endDate > :startDate
        """)
        boolean isRoomAvailableForPeriod(
                @Param("room") Room room,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );

        @Query("""
                SELECT COUNT(r) = 0
                FROM Reservation r
                WHERE r.room = :room
                AND r.startDate < :endDate
                AND r.endDate > :startDate
                AND r.id <> :reservationId
        """)
        boolean isRoomAvailableForPeriodWithReservation(
                @Param("reservationId") UUID reservationId,
                @Param("room") Room room,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT r FROM Reservation r WHERE r.id = :id")
        java.util.Optional<Reservation> findByIdForUpdate(
                @Param("id") UUID id
        );
}
