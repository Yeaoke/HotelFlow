package com.example.app.security.jwt.Token.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.app.models.User;
import com.example.app.security.jwt.Token.model.Token;

public interface TokenRepository extends JpaRepository<Token, User> {

    @Query("""
        SELECT t
        FROM Token t
        INNER JOIN User u
        on t.user.id = u.id
        WHERE t.user.id = :userId and t.loggedOut = false
    """)
    List<Token> findAllAccessTokens(UUID userId);

    Optional<Token> findByAccessToken(String accessToken);

    Optional<Token> findByRefreshToken(String refreshToken);
}
