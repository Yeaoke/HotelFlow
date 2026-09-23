package com.example.app.repos.token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.app.models.main.User;
import com.example.app.models.token.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, User> {

    @Query(
        value = """
            SELECT t.userId
            FROM reservation_info.Users u
            JOIN token_info.Tokens t
            ON t.user_id = u.id
            WHERE t.user_id = :userId 
        """,
        nativeQuery = true
    )
    List<Token> findAllAccessTokens(UUID userId);

    Optional<Token> findByAccessToken(String accessToken);

    Optional<Token> findByRefreshToken(String refreshToken);
}
