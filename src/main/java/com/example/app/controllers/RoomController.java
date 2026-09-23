package com.example.app.controllers;

import com.example.app.dto.room.output.RoomResponse;
import com.example.app.models.main.Room;
import com.example.app.services.RoomService;
import com.example.app.dto.room.input.RoomRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getListOfAllRooms() {
        List<Room> rooms = roomService.getAllRooms();

        List<RoomResponse> roomsResponse = rooms.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roomsResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable UUID id) {
        Optional<Room> room = roomService.getRoomById(id);

        if (room.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToResponseDTO(room.get()));
    }

    @Profile("dev")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        if (roomService.getRoomById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @Profile("dev")
    @PostMapping("/create/{userId}")
    public ResponseEntity<Room> createRoomForReservation(
            @PathVariable UUID userId,
            @RequestBody @Valid RoomRequest dto) {
        
        try {
            Room room = roomService.createRoom(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private RoomResponse convertToResponseDTO(Room room) {
        return new RoomResponse(
                room.getHomeType(),
                room.getAddress(),
                room.getHasTV(),
                room.getHasInternet(),
                room.getHasKitchen(),
                room.getHasAirCon(),
                room.getPrice(),
                room.getLatitude(),
                room.getLongitude()
        );
    }
}