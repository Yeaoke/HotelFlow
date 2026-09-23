package com.example.app.repos.main;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.app.models.main.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
        SELECT DISTINCT r.user
        FROM Reservation r
        JOIN Review rev ON rev.reservation = r
    """)
    List<User> findAllUsersWhoMadeReview();
    
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
