package com.example.app.mappers;
import com.example.app.models.main.Room;
import com.example.app.dto.room.input.RoomRequest;
import com.example.app.dto.room.output.RoomResponse;
public final class RoomMapper {

    private RoomMapper() {}

    public static RoomResponse toDto(
        Room r
    ) {
        return new RoomResponse(
            r.getId(),
            r.getHomeType(),
            r.getAddress(),
            r.getHasTV(),
            r.getHasInternet(),
            r.getHasKitchen(),
            r.getHasAirCon(),
            r.getPrice(),
            r.getLatitude(),
            r.getLongitude()
        );
    }

    public static void apply(Room r,RoomRequest d) {
        r.setHomeType(d.homeType());
        r.setAddress(d.address());
        r.setHasTV(d.hasTV());
        r.setHasKitchen(d.hasKitchen());
        r.setHasInternet(d.hasInternet());
        r.setHasAirCon(d.hasAirCon());
        r.setPrice(d.price());
        r.setLatitude(d.latitude());
        r.setLongitude(d.longitude());
    }
}
