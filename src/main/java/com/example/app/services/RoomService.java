package com.example.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.dto.room.input.RoomRequest;
import com.example.app.exceptions.RoomNotFoundException;
import com.example.app.exceptions.UserNotFoundException;
import com.example.app.models.main.Room;
import com.example.app.models.main.User;
import com.example.app.repos.main.RoomRepository;
import com.example.app.repos.main.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public Room createRoom(
            RoomRequest dto,
            UUID userId
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Room data can't be null"
            );
        }

        validateUserId(userId);
        validatePrice(dto.price());

        User owner = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + userId)
                );

        log.info(
                "Creating room: ownerId={}, thread={}",
                userId,
                Thread.currentThread().getName()
        );

        Room room = new Room();

        room.setHomeType(dto.homeType());
        room.setAddress(dto.address());
        room.setHasTV(dto.hasTV());
        room.setHasInternet(dto.hasInternet());
        room.setHasKitchen(dto.hasKitchen());
        room.setHasAirCon(dto.hasAirCon());
        room.setPrice(dto.price());
        room.setOwner(owner);
        room.setLatitude(dto.latitude());
        room.setLongitude(dto.longitude());

        Room savedRoom = roomRepository.save(room);

        log.info(
                "Room created: id={}, ownerId={}",
                savedRoom.getId(),
                userId
        );

        return savedRoom;
    }

    @Transactional(readOnly = true)
    public Optional<Room> getRoomById(UUID id) {

        if (id == null) {
            return Optional.empty();
        }

        return roomRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional
    public void deleteRoom(
            UUID id,
            UUID userId
    ) {
        if (id == null) {
            throw new RoomNotFoundException(
                    "Room id can't be null"
            );
        }

        validateUserId(userId);

        log.info(
                "Deleting room: id={}, userId={}",
                id,
                userId
        );

        Room room = roomRepository.findById(id)
                .orElseThrow(
                        () -> new RoomNotFoundException("Room not found with id: " + id)
                );

        checkOwnership(room, userId);

        roomRepository.delete(room);

        log.info(
                "Room deleted: id={}, userId={}",
                id,
                userId
        );
    }

    private void checkOwnership(
            Room room,
            UUID userId
    ) {
        if (room.getOwner() == null
                || room.getOwner().getId() == null
                || !room.getOwner().getId().equals(userId)) {

            throw new AccessDeniedException(
                    "You don't have access to this room"
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

    private void validatePrice(Long price) {

        if (price == null) {
            throw new IllegalArgumentException(
                    "Price can't be null"
            );
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "Price can't be less than 0"
            );
        }
    }
}