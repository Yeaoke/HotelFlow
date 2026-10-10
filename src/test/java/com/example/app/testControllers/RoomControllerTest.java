package com.example.app.testControllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.app.controllers.RoomController;
import com.example.app.dto.room.input.RoomRequest;
import com.example.app.models.main.Room;
import com.example.app.services.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RoomController roomController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID roomId;
    private UUID userId;

    private Room room;
    private RoomRequest roomRequest;

    private String address;
    private String homeType;
    private Long price;
    private Double latitude;
    private Double longitude;
    private boolean hasTV;
    private boolean hasKitchen;
    private boolean hasInternet;
    private boolean hasAirCon;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(roomController)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();

        roomId = UUID.randomUUID();
        userId = UUID.randomUUID();

        address = "Moscow, Tverskaya Street, 10";
        homeType = "APARTMENT";
        price = 5000L;
        latitude = 55.7558;
        longitude = 37.6173;
        hasTV = true;
        hasKitchen = true;
        hasInternet = false;
        hasAirCon = true;

        roomRequest = new RoomRequest(
                homeType,
                address,
                hasTV,
                hasKitchen,
                hasInternet,
                hasAirCon,
                price,
                userId,
                latitude,
                longitude
        );

        room = new Room();
        room.setId(roomId);
        room.setHomeType(homeType);
        room.setAddress(address);
        room.setHasTV(hasTV);
        room.setHasKitchen(hasKitchen);
        room.setHasInternet(hasInternet);
        room.setHasAirCon(hasAirCon);
        room.setPrice(price);
        room.setLatitude(latitude);
        room.setLongitude(longitude);
    }

    @Test
    void getAllRooms_true() throws Exception {
        when(roomService.getAllRooms())
                .thenReturn(List.of(room));

        mockMvc.perform(get("/api/rooms"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].address").value(address))
                .andExpect(jsonPath("$[0].homeType").value(homeType))
                .andExpect(jsonPath("$[0].price").value(price));

        verify(roomService).getAllRooms();
    }

    @Test
    void getAllRooms_emptyList() throws Exception {
        when(roomService.getAllRooms())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(roomService).getAllRooms();
    }

    @Test
    void getRoomById_true() throws Exception {
        when(roomService.getRoomById(roomId))
                .thenReturn(Optional.of(room));

        mockMvc.perform(get("/api/rooms/{id}", roomId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.homeType").value(homeType))
                .andExpect(jsonPath("$.price").value(price));

        verify(roomService).getRoomById(roomId);
    }

    @Test
    void getRoomById_notFound() throws Exception {
        when(roomService.getRoomById(roomId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rooms/{id}", roomId))
                .andExpect(status().isNotFound());

        verify(roomService).getRoomById(roomId);
    }

    @Test
    void createRoom_true() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        when(roomService.createRoom(
                any(RoomRequest.class),
                eq(userId)
        )).thenReturn(room);

        mockMvc.perform(
                        post("/api/rooms/create")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(roomRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.homeType").value(homeType))
                .andExpect(jsonPath("$.price").value(price));

        verify(roomService).createRoom(
                any(RoomRequest.class),
                eq(userId)
        );
    }

    @Test
    void deleteRoom_true() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        mockMvc.perform(
                        delete("/api/rooms/{id}", roomId)
                                .principal(authentication)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(roomService).deleteRoom(roomId, userId);
    }
}