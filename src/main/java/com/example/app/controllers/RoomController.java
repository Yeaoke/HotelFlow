package com.example.app.controllers;

import com.example.app.dto.room.input.RoomRequest;
import com.example.app.dto.room.output.RoomResponse;
import com.example.app.models.main.Room;
import com.example.app.services.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rooms")
@SecurityRequirement(name = "Bearer Authentication")
public class RoomController {

    private final RoomService roomService;

    @Operation(
            summary = "Получить список всех комнат",
            description = "Возвращает список доступных комнат"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Комнаты успешно получены"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {

        List<RoomResponse> rooms = roomService.getAllRooms()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();

        return ResponseEntity.ok(rooms);
    }

    @Operation(
            summary = "Получить комнату по ID",
            description = "Возвращает информацию о комнате"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Комната найдена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Комната не найдена"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(
            @PathVariable UUID id
    ) {

        return roomService.getRoomById(id)
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Создать комнату",
            description = "Создаёт комнату от имени текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Комната успешно создана"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные комнаты"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            Authentication authentication,
            @RequestBody @Valid RoomRequest dto
    ) {

        UUID userId = getUserId(authentication);

        Room room = roomService.createRoom(dto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertToResponseDTO(room));
    }

    @Operation(
            summary = "Удалить комнату",
            description = "Удаляет комнату текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Комната успешно удалена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет доступа к комнате"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Комната не найдена"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        roomService.deleteRoom(id, userId);

        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
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