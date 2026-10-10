package com.example.app.dto.room.input;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(

    @NotBlank(message = "homeType cannot be empty")
    String homeType,

    @NotBlank(message = "address can't be empty")
    String address,

    @NotNull(message = "room must have info about TV")
    Boolean hasTV,

    @NotNull(message = "room must have info about kitchen")
    Boolean hasKitchen,

    @NotNull(message = "room must have info about Internet")
    Boolean hasInternet,

    @NotNull(message = "room must have info about Air con")
    Boolean hasAirCon,

    @NotNull(message = "price must be provided")
    Long price,

    @NotNull(message = "user id can't be null")
    UUID userId,

    @NotNull(message = "latitude must be provided")
    Double latitude,

    @NotNull(message = "longitude must be provided")
    Double longitude
) {}