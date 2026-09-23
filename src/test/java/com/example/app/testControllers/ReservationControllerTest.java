package com.example.app.testControllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.app.controllers.ReservationController;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.models.main.Reservation;
import com.example.app.models.main.Room;
import com.example.app.models.main.User;
import com.example.app.services.ReservationService;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {
    
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
    }

    @Test
    void getReservationById_true() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(4);

        User user = new User();
        user.setId(userId);

        Room room = new Room();
        room.setId(roomId);
        room.setPrice(1000L);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setRoom(room);
        reservation.setUser(user);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);

        when(reservationService.findReservation(reservationId))
                .thenReturn(Optional.of(reservation));

        mockMvc.perform(get("/api/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.toString()));

        verify(reservationService, times(1)).findReservation(reservationId);
    }

    @Test
    void createReservation_true() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(4);

        User user = new User();
        user.setId(userId);

        Room room = new Room();
        room.setId(roomId);
        room.setPrice(1000L);

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setPrice(3000L);

        ReservationRequest dto =
                new ReservationRequest(
                        roomId,
                        startDate,
                        endDate
                );

        when(reservationService.createReservation(dto, userId))
                .thenReturn(reservation);

        mockMvc.perform(post("/api/reservations/create/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "roomId": "%s",
                                "start_date": "%s",
                                "end_date": "%s"
                            }
                            """.formatted(
                                roomId,
                                startDate,
                                endDate
                        ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId")
                        .value(roomId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.startDate")
                        .value(startDate.toString()))
                .andExpect(jsonPath("$.endDate")
                        .value(endDate.toString()));

        verify(reservationService, times(1))
                .createReservation(dto, userId);
    }
}
