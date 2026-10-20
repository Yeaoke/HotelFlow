package com.example.app.services;
import com.example.app.dto.ReservationStatus;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.dto.reservation.output.ReservationResponse;
import com.example.app.exceptions.*;
import com.example.app.models.main.*;
import com.example.app.repos.main.*;
import com.example.app.mappers.ReservationMapper;
import com.example.app.policies.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservations;
    private final UserRepository users;
    private final RoomRepository rooms;

    @Transactional
    public ReservationResponse createReservation(
        ReservationRequest dto,
        UUID userId
    ) {
        ReservationPolicy.validateDates(dto);

        User user = users.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        Room room = lockRoom(dto.roomId());

        if (!reservations.isRoomAvailableForPeriod(room,dto.start_date(),dto.end_date())) {
            throw new RoomAlreadyReservedException("Room is already reserved for these dates");
        }

        Reservation r = new Reservation();
        
        r.setUser(user);
        r.setStatus(ReservationStatus.APPROVED);

        apply(r,room,dto);

        return ReservationMapper.toDto(reservations.saveAndFlush(r));
    }

    @Transactional
    public ReservationResponse updateReservation(
        UUID id,
        ReservationRequest dto,
        UUID userId
    ) {
        ReservationPolicy.validateDates(dto);

        Reservation r = ownedLocked(id,userId);

        ReservationPolicy.requireEditable(r);

        UUID old = r.getRoomId(),
        next=dto.roomId();

        List<UUID> ids=new ArrayList<>(new HashSet<>(List.of(old,next)));

        ids.sort(UUID::compareTo);

        Room target = null;
        for (UUID roomId:ids) {
            Room room = lockRoom(roomId);
            if (roomId.equals(next))
                target=room;
            }

        if (!reservations.isRoomAvailableForPeriodWithReservation(id, target, dto.start_date(), dto.end_date())) {
            throw new RoomAlreadyReservedException("Room is already reserved for these dates");
        }

        apply(r,target,dto);

        return ReservationMapper.toDto(reservations.saveAndFlush(r));
    }

    @Transactional
    public ReservationResponse updateReservationStatus(
        UUID id,
        ReservationStatus status,
        UUID userId
    ) {
        Reservation r = ownedLocked(id,userId);

        ReservationPolicy.transition(r,status);

        lockRoom(r.getRoomId());

        if (status == ReservationStatus.APPROVED && r.getStatus() != status && !reservations.isRoomAvailableForPeriodWithReservation(id, r.getRoom(), r.getStartDate(), r.getEndDate())) {
            throw new RoomAlreadyReservedException("Room is already reserved");
        }

        r.setStatus(status);

        return ReservationMapper.toDto(reservations.saveAndFlush(r));
    }

    @Transactional(readOnly=true)
    public ReservationResponse getReservationById(
        UUID id,
        UUID userId
    ) {
        Reservation r = reservations.findById(id).orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        checkOwner(r,userId);
        
        return ReservationMapper.toDto(r);
    }

    @Transactional(readOnly=true)
    public List<ReservationResponse> getAllReservations(
        UUID userId
    ) {
        return reservations.findAllUserReservations(userId).stream().map(ReservationMapper::toDto).toList();
    }

    @Transactional public void deleteReservation(
        UUID id,
        UUID userId
    ) {
        updateReservationStatus(id,ReservationStatus.CANCELED,userId);
    }

    private Reservation ownedLocked(
        UUID id,
        UUID userId
    ) {
        Reservation r = reservations.findByIdForUpdate(id).orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        checkOwner(r,userId);

        return r;
    }

    private Room lockRoom(
        UUID id
    ) {
        return rooms.findByIdForUpdate(id).orElseThrow(() -> new RoomNotFoundException("Room not found"));
    }

    private void checkOwner(
        Reservation r,
        UUID id
    ) {
        if (!r.getUserId().equals(id)) {
            throw new AccessDeniedException("Reservation belongs to another user");
        }
    }

    private void apply(
        Reservation r,
        Room room,
        ReservationRequest dto
    ) {
        r.setRoom(room);
        r.setStartDate(dto.start_date());
        r.setEndDate(dto.end_date());
        r.setPrice(PriceCalculator.calculate(room.getPrice(), dto.start_date(), dto.end_date()));
    }
}
