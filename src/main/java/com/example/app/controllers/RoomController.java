package com.example.app.controllers;
import com.example.app.dto.room.input.RoomRequest;
import com.example.app.dto.room.output.RoomResponse;
import com.example.app.dto.PageResponse;
import com.example.app.services.RoomService;
import com.example.app.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import java.util.UUID;
@RestController @RequestMapping("/api/rooms") @RequiredArgsConstructor
@SecurityRequirement(name="Bearer Authentication")
public class RoomController {

    private final RoomService service;

    @GetMapping
    public PageResponse<RoomResponse> search(
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="price") String sort,@RequestParam(defaultValue="asc") String direction,
        @RequestParam(required=false) String homeType,@RequestParam(required=false) String address,
        @RequestParam(required=false) Long minPrice,@RequestParam(required=false) Long maxPrice,
        @RequestParam(required=false) Boolean internet,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.search(page, size, sort, direction, homeType, address, minPrice, maxPrice, internet, start, end);
    }

    @GetMapping("/{id}")
    public RoomResponse get(
        @PathVariable UUID id
    ) {
        return service.getRoomById(id);
    }
    
    @PostMapping
    public ResponseEntity<RoomResponse> create(
        @Valid @RequestBody RoomRequest dto,
        Authentication auth
    ) {
        var result = service.createRoom(dto,CurrentUser.id(auth));

        return ResponseEntity.created(java.net.URI.create("/api/rooms/"+result.id())).body(result);
    }

    @PutMapping("/{id}")
    public RoomResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody RoomRequest dto,
        Authentication auth
    ) {
        return service.updateRoom(id,dto,CurrentUser.id(auth));
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable UUID id,
        Authentication auth
    ) {
        service.deleteRoom(id,CurrentUser.id(auth));
    }
}
