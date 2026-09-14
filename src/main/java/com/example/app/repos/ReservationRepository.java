package com.example.app.repos;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.app.models.Reservation;
import com.example.app.models.Room;

import jakarta.persistence.LockModeType;

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

        @Modifying
        @Query("""
                UPDATE Reservation r
                SET r.status = 'EXPIRED'
                WHERE r.endDate < :endDate
                AND r.status = 'ACTIVE'
        """)
        int findExpiredReservation(
                @Param("endDate") LocalDate endDate
        );
}
