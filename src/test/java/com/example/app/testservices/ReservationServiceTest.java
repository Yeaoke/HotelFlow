package com.example.app.testservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.app.dto.ReservationStatus;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.exceptions.DaysAmountException;
import com.example.app.models.main.Reservation;
import com.example.app.models.main.Room;
import com.example.app.models.main.User;
import com.example.app.repos.main.ReservationRepository;
import com.example.app.repos.main.RoomRepository;
import com.example.app.repos.main.UserRepository;
import com.example.app.services.ReservationService;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void deleteReservation_true() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Room room = new Room();
        room.setId(roomId);
        room.setPrice(1000L);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setUser(user);
        reservation.setRoom(room);

        when(reservationRepository.findByIdForUpdate(reservationId))
                .thenReturn(Optional.of(reservation));
        when(roomRepository.findByIdForUpdate(roomId))
                .thenReturn(Optional.of(room));

        reservationService.deleteReservation(reservationId, userId);

        verify(reservationRepository, times(1)).findByIdForUpdate(reservationId);
        verify(roomRepository, times(1)).findByIdForUpdate(roomId);
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    void updateReservation_true() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        ReservationRequest dto = new ReservationRequest(roomId, start, end);

        User user = new User();
        user.setId(userId);

        Room room = new Room();
        room.setId(roomId);
        room.setPrice(1000L);

        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setStartDate(LocalDate.now().plusDays(10));
        reservation.setEndDate(LocalDate.now().plusDays(12));

        when(reservationRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(reservation));
        when(roomRepository.findByIdForUpdate(roomId))
                .thenReturn(Optional.of(room));
        when(reservationRepository.isRoomAvailableForPeriodWithReservation(
                id, room, start, end))
                .thenReturn(true);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.updateReservation(id, dto, userId);

        assertNotNull(result);
        assertEquals(start, result.getStartDate());
        assertEquals(end, result.getEndDate());
        assertEquals(roomId, result.getRoom().getId());
        assertEquals(2000L, result.getPrice()); // 1000 * 3 дня

        verify(reservationRepository).findByIdForUpdate(id);
        verify(roomRepository).findByIdForUpdate(roomId);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void createReservation_true() {
        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(4);

        ReservationRequest dto = new ReservationRequest(roomId, startDate, endDate);

        User user = new User();
        user.setId(userId);

        Room room = new Room();
        room.setId(roomId);
        room.setPrice(1000L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(roomRepository.findByIdForUpdate(roomId))
                .thenReturn(Optional.of(room));
        when(reservationRepository.isRoomAvailableForPeriod(room, startDate, endDate))
                .thenReturn(true);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(dto, userId);

        assertNotNull(result);
        assertEquals(roomId, result.getRoom().getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertEquals(3000L, result.getPrice()); // 1000 * 3 дня
        assertEquals(ReservationStatus.APPROVED, result.getStatus());

        verify(userRepository, times(1)).findById(userId);
        verify(roomRepository, times(1)).findByIdForUpdate(roomId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void createReservation_failedNotValidDates() {
        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(1);

        ReservationRequest dto = new ReservationRequest(roomId, startDate, endDate);

        assertThrows(
                DaysAmountException.class,
                () -> reservationService.createReservation(dto, userId)
        );

        verify(userRepository, never()).findById(any());
        verify(roomRepository, never()).findByIdForUpdate(any());
        verify(reservationRepository, never()).save(any());
    }
}