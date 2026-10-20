package com.example.app.repos.main;
import com.example.app.models.main.Review;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReviewRepository extends JpaRepository<Review,UUID> {
    List<Review> findAllByReservationId(UUID reservationId);
    Optional<Review> findByIdAndReservationId(UUID id,UUID reservationId);
    boolean existsByReservationId(UUID reservationId);
}
