package com.example.app.repos.main;
import com.example.app.models.main.Room;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RoomRepository extends JpaRepository<Room,UUID>,JpaSpecificationExecutor<Room> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id=:id")
    Optional<Room> findByIdForUpdate(@Param("id") UUID id);
}
