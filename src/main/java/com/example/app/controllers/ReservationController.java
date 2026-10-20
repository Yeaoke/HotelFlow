package com.example.app.controllers;
import com.example.app.dto.ReservationStatus;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.dto.reservation.output.ReservationResponse;
import com.example.app.services.ReservationService;
import com.example.app.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.*;
@RestController @RequestMapping("/api/reservations") @RequiredArgsConstructor
@SecurityRequirement(name="Bearer Authentication")
public class ReservationController {
    
    private final ReservationService service;
    
    @GetMapping
    public List<ReservationResponse> all(
        Authentication auth
    ) {
        return service.getAllReservations(CurrentUser.id(auth));
    }
    
    @GetMapping("/{id}")
    public ReservationResponse get(
        @PathVariable UUID id,
        Authentication auth
    ) {
        return service.getReservationById(id, CurrentUser.id(auth));
    }
    
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
        @Valid @RequestBody ReservationRequest dto,
        Authentication auth
    ) {
        var result = service.createReservation(dto,CurrentUser.id(auth));

        return ResponseEntity.created(java.net.URI.create("/api/reservations/"+result.id())).body(result);
    }
    
    @PutMapping("/{id}")
    public ReservationResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody ReservationRequest dto,
        Authentication auth
    ) {
        return service.updateReservation(id, dto,CurrentUser.id(auth));
    }
    
    @PatchMapping("/{id}/status")
    public ReservationResponse status(@PathVariable UUID id,@RequestParam ReservationStatus status,Authentication auth) {
        return service.updateReservationStatus(id,status,CurrentUser.id(auth));
    }
    
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable UUID id,
        Authentication auth
    ) {
        service.deleteReservation(id,CurrentUser.id(auth));
    }
}
