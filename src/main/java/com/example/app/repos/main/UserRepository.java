package com.example.app.repos.main;
import com.example.app.models.main.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface UserRepository extends JpaRepository<User,UUID> {
    @Query("select distinct r.user from Reservation r join Review rev on rev.reservation = r")
    List<User> findAllUsersWhoMadeReview();
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsernameForUpdate(@Param("username") String username);
}
