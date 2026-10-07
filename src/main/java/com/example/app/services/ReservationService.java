package com.example.app.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.dto.ReservationStatus;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.exceptions.DaysAmountException;
import com.example.app.exceptions.ReservationNotFoundException;
import com.example.app.exceptions.RoomAlreadyReservedException;
import com.example.app.exceptions.RoomNotFoundException;
import com.example.app.exceptions.UserNotFoundException;
import com.example.app.models.main.Reservation;
import com.example.app.models.main.Room;
import com.example.app.models.main.User;
import com.example.app.repos.main.ReservationRepository;
import com.example.app.repos.main.RoomRepository;
import com.example.app.repos.main.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 30;

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public Reservation createReservation(
            ReservationRequest dto,
            UUID userId
    ) {
        validateDates(dto);

        if (userId == null) {
            throw new UserNotFoundException(
                    "User id can't be null"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + userId)
                );

        Room room = roomRepository.findByIdForUpdate(dto.roomId())
                .orElseThrow(
                        () -> new RoomNotFoundException("Room not found with id: " + dto.roomId())
                );

        boolean available =
                reservationRepository.isRoomAvailableForPeriod(
                        room,
                        dto.start_date(),
                        dto.end_date()
                );

        if (!available) {
            throw new RoomAlreadyReservedException(
                    "Room already reserved for this period"
            );
        }

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setStartDate(dto.start_date());
        reservation.setEndDate(dto.end_date());
        reservation.setPrice(
                calculatePrice(
                        room,
                        dto.start_date(),
                        dto.end_date()
                )
        );
        reservation.setStatus(ReservationStatus.APPROVED);

        Reservation saved = reservationRepository.save(reservation);

        log.info(
                "Reservation created: reservationId={}, roomId={}, userId={}, thread={}",
                saved.getId(),
                room.getId(),
                userId,
                Thread.currentThread().getName()
        );

        return saved;
    }

    @Transactional
    public Reservation updateReservation(
            UUID reservationId,
            ReservationRequest dto,
            UUID userId
    ) {
        if (reservationId == null) {
            throw new ReservationNotFoundException(
                    "Reservation id can't be null"
            );
        }

        validateUserId(userId);
        validateDates(dto);

        Reservation reservation = reservationRepository.findByIdForUpdate(
                reservationId
        ).orElseThrow(() -> new ReservationNotFoundException(
                "Reservation not found with id: " + reservationId
        ));

        checkOwner(reservation, userId);

        UUID oldRoomId = reservation.getRoom().getId();
        UUID newRoomId = dto.roomId();

        if (oldRoomId.equals(newRoomId)) {

            Room room = roomRepository.findByIdForUpdate(oldRoomId)
                    .orElseThrow(() -> new RoomNotFoundException(
                            "Room not found with id: " + oldRoomId
                    ));

            checkAvailable(
                    reservationId,
                    room,
                    dto.start_date(),
                    dto.end_date()
            );

            applyReservationChanges(
                    reservation,
                    room,
                    dto.start_date(),
                    dto.end_date()
            );

            Reservation saved = reservationRepository.save(reservation);

            log.info(
                    "Reservation updated: reservationId={}, roomId={}, userId={}, thread={}",
                    reservationId,
                    room.getId(),
                    userId,
                    Thread.currentThread().getName()
            );

            return saved;
        }

        UUID firstId;
        UUID secondId;

        if (oldRoomId.compareTo(newRoomId) < 0) {
            firstId = oldRoomId;
            secondId = newRoomId;
        } else {
            firstId = newRoomId;
            secondId = oldRoomId;
        }

        Room firstRoom = roomRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new RoomNotFoundException(
                        "Room not found with id: " + firstId
                ));

        Room secondRoom = roomRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new RoomNotFoundException(
                        "Room not found with id: " + secondId
                ));

        Room newRoom;

        if (newRoomId.equals(firstId)) {
            newRoom = firstRoom;
        } else {
            newRoom = secondRoom;
        }

        checkAvailable(
                reservationId,
                newRoom,
                dto.start_date(),
                dto.end_date()
        );

        applyReservationChanges(
                reservation,
                newRoom,
                dto.start_date(),
                dto.end_date()
        );

        Reservation saved = reservationRepository.save(reservation);

        log.info(
                "Reservation updated: reservationId={}, oldRoomId={}, newRoomId={}, userId={}, thread={}",
                reservationId,
                oldRoomId,
                newRoomId,
                userId,
                Thread.currentThread().getName()
        );

        return saved;
    }

    @Transactional
    public Reservation updateReservationStatus(
            UUID id,
            ReservationStatus status,
            UUID userId
    ) {
        if (id == null) {
            throw new ReservationNotFoundException(
                    "Reservation id can't be null"
            );
        }

        validateUserId(userId);

        if (status == null) {
            throw new IllegalArgumentException(
                    "Status can't be null"
            );
        }

        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(
                        () -> new ReservationNotFoundException("Reservation not found with id: " + id)
                );

        checkOwner(reservation, userId);

        reservation.setStatus(status);

        Reservation saved = reservationRepository.save(reservation);

        log.info(
                "Reservation status updated: reservationId={}, status={}, userId={}",
                id,
                status,
                userId
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Reservation> findReservation(
            UUID id,
            UUID userId
    ) {
        if (id == null) {
            return Optional.empty();
        }

        validateUserId(userId);

        Optional<Reservation> reservation = reservationRepository.findById(id);

        if (reservation.isEmpty()) {
            return Optional.empty();
        }

        checkOwner(reservation.get(), userId);

        return reservation;
    }

    @Transactional(readOnly = true)
    public Optional<Reservation> getReservationById(
            UUID id,
            UUID userId
    ) {
        return findReservation(id, userId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations(UUID userId) {
        validateUserId(userId);

        return reservationRepository.findAllUserReservations(userId);
    }

    @Transactional
    public void deleteReservation(
            UUID id,
            UUID userId
    ) {
        if (id == null) {
            throw new ReservationNotFoundException(
                    "Reservation id can't be null"
            );
        }

        validateUserId(userId);

        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with id: " + id
                ));

        checkOwner(reservation, userId);

        UUID roomId = reservation.getRoom().getId();

        roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new RoomNotFoundException(
                        "Room not found with id: " + roomId
                ));

        reservationRepository.delete(reservation);

        log.info(
                "Reservation deleted: reservationId={}, roomId={}, userId={}, thread={}",
                id,
                roomId,
                userId,
                Thread.currentThread().getName()
        );
    }

    private void checkOwner(
            Reservation reservation,
            UUID userId
    ) {
        if (reservation.getUser() == null || reservation.getUser().getId() == null || !reservation.getUser().getId().equals(userId)) {

            throw new AccessDeniedException(
                    "You don't have access to this reservation"
            );
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new UserNotFoundException(
                    "User id can't be null"
            );
        }
    }

    private void checkAvailable(
            UUID reservationId,
            Room room,
            LocalDate startDate,
            LocalDate endDate
    ) {
        boolean available =
                reservationRepository
                        .isRoomAvailableForPeriodWithReservation(
                                reservationId,
                                room,
                                startDate,
                                endDate
                        );

        if (!available) {
            throw new RoomAlreadyReservedException(
                    "Room already reserved for this period"
            );
        }
    }

    private void applyReservationChanges(
            Reservation reservation,
            Room room,
            LocalDate startDate,
            LocalDate endDate
    ) {
        reservation.setRoom(room);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setPrice(
                calculatePrice(
                        room,
                        startDate,
                        endDate
                )
        );
    }

    private Long calculatePrice(
            Room room,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (room == null) {
            throw new RoomNotFoundException(
                    "Room can't be null"
            );
        }

        if (room.getPrice() == null) {
            throw new IllegalArgumentException(
                    "Room price can't be null"
            );
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date can't be null"
            );
        }

        long days = ChronoUnit.DAYS.between(
                startDate,
                endDate
        );

        try {
            return Math.multiplyExact(
                    room.getPrice(),
                    days
            );
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Reservation price is too large",
                    e
            );
        }
    }

    private void validateDates(ReservationRequest dto) {

        if (dto == null) {
            throw new DaysAmountException(
                    "Reservation data can't be null"
            );
        }

        if (dto.roomId() == null) {
            throw new DaysAmountException(
                    "Room id can't be null"
            );
        }

        if (dto.start_date() == null || dto.end_date() == null) {
            throw new DaysAmountException(
                    "Start date and end date can't be null"
            );
        }

        if (!dto.start_date().isBefore(dto.end_date())) {
            throw new DaysAmountException(
                    "Start date must be before end date"
            );
        }

        long days = ChronoUnit.DAYS.between(
                dto.start_date(),
                dto.end_date()
        );

        if (days < MIN_DAYS) {
            throw new DaysAmountException(
                    "Reservation must be at least 1 day"
            );
        }

        if (days > MAX_DAYS) {
            throw new DaysAmountException(
                    "Reservation cannot be longer than 30 days"
            );
        }
    }
}