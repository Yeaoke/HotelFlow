package com.example.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.app.dto.room.input.RoomRequest;
import com.example.app.exceptions.RoomNotFoundException;
import com.example.app.exceptions.UserNotFoundException;
import com.example.app.models.main.Room;
import com.example.app.models.main.User;
import com.example.app.repos.main.RoomRepository;
import com.example.app.repos.main.UserRepository;

import jakarta.transaction.Transactional;
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

        log.info("Creating room: ownerId={}, thread={}", userId, Thread.currentThread().getName());

        validatePrice(dto.price());

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                                "User not found with id: " + userId
                        )
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

        log.info("Room created: id={}, ownerId={}", savedRoom.getId(), userId);

        return savedRoom;
    }

    public Optional<Room> getRoomById(UUID id) {

        log.info("Getting room: id={}", id);

        return roomRepository.findById(id);
        }

    public List<Room> getAllRooms() {

        log.info("Getting all rooms");

        return roomRepository.findAll();
    }

    @Transactional
    public void deleteRoom(UUID id) {
        log.info("Deleting room: id={}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(
                                "Room not found with id: " + id
                        )
                );
        roomRepository.delete(room);
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