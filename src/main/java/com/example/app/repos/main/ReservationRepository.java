package com.example.app.repos.main;
import java.time.LocalDate;
import java.util.*;
import com.example.app.models.main.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface ReservationRepository extends JpaRepository<Reservation,UUID> {
    @Query("""
        select count(r)=0 from Reservation r where r.room=:room
        and r.status <> com.example.app.dto.ReservationStatus.CANCELED
        and r.startDate<:endDate and r.endDate>:startDate
        """)
    boolean isRoomAvailableForPeriod(@Param("room") Room room,@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
    @Query("""
        select count(r)=0 from Reservation r where r.room=:room
        and r.status <> com.example.app.dto.ReservationStatus.CANCELED
        and r.startDate<:endDate and r.endDate>:startDate and r.id<>:reservationId
        """)
    boolean isRoomAvailableForPeriodWithReservation(@Param("reservationId") UUID id,@Param("room") Room room,
        @Param("startDate") LocalDate start,@Param("endDate") LocalDate end);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id=:id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);
    @Query("select r from Reservation r where r.user.id=:userId order by r.startDate desc, r.id")
    List<Reservation> findAllUserReservations(@Param("userId") UUID userId);
}
