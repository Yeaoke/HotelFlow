package com.example.app.dto.room.input;
import jakarta.validation.constraints.*;
public record RoomRequest(
    @NotBlank
    @Size(max=100)
    String homeType,

    @NotBlank
    @Size(max=255)
    String address,

    @NotNull
    Boolean hasTV,

    @NotNull
    Boolean hasKitchen,

    @NotNull
    Boolean hasInternet,

    @NotNull
    Boolean hasAirCon,

    @NotNull
    @PositiveOrZero
    Long price,

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    Double latitude,

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    Double longitude
) {}
