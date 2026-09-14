package com.example.app.repos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.app.models.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
        SELECT DISTINCT r.user
        FROM Reservation r
        JOIN Review rev ON rev.reservation = r
    """)
    List<User> findAllUsersWhoMadeReview();
    
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
