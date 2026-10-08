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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.app.controllers.ReservationController;
import com.example.app.dto.ReservationStatus;
import com.example.app.dto.reservation.input.ReservationRequest;
import com.example.app.models.main.Reservation;
import com.example.app.models.main.Room;
import com.example.app.models.main.User;
import com.example.app.services.ReservationService;
import com.example.app.services.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID roomId;
    private UUID reservationId;

    @BeforeEach
    void setUp() {
        objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .build();

        mockMvc = MockMvcBuilders
                .standaloneSetup(reservationController)
                .build();

        userId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
    }

    @Test
    void getReservationById_true() throws Exception {
        when(authentication.getName()).thenReturn(userId.toString());

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
        reservation.setPrice(3000L);
        reservation.setStatus(ReservationStatus.APPROVED);

        when(reservationService.findReservation(reservationId, userId))
                .thenReturn(Optional.of(reservation));

        mockMvc.perform(
                        get("/api/reservations/{id}", reservationId)
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.toString()))
                .andExpect(jsonPath("$.price").value(3000L));

        verify(reservationService, times(1))
                .findReservation(reservationId, userId);
    }

    @Test
    void getReservationById_notFound() throws Exception {
        when(authentication.getName()).thenReturn(userId.toString());

        when(reservationService.findReservation(reservationId, userId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/reservations/{id}", reservationId)
                                .principal(authentication)
                )
                .andExpect(status().isNotFound());

        verify(reservationService, times(1))
                .findReservation(reservationId, userId);
    }

    @Test
    void createReservation_true() throws Exception {
        when(authentication.getName()).thenReturn(userId.toString());

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(4);

        User user = new User();
        user.setId(userId);

        Room room = new Room();
        room.setId(roomId);
        room.setPrice(1000L);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setPrice(3000L);
        reservation.setStatus(ReservationStatus.APPROVED);

        ReservationRequest dto = new ReservationRequest(roomId, startDate, endDate);

        when(reservationService.createReservation(any(ReservationRequest.class), eq(userId)))
                .thenReturn(reservation);

        mockMvc.perform(
                        post("/api/reservations")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.toString()))
                .andExpect(jsonPath("$.price").value(3000L));

        verify(reservationService, times(1))
                .createReservation(any(ReservationRequest.class), eq(userId));
    }
}