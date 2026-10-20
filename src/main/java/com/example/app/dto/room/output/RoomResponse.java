package com.example.app.dto.room.output;
import java.util.UUID;
public record RoomResponse(
    UUID id,

    String homeType,

    String address,

    Boolean hasTV,

    Boolean hasInternet,

    Boolean hasKitchen,

    Boolean hasAirCon,

    Long price,

    Double latitude,
    
    Double longitude
) {}
