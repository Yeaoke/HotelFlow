package com.example.app.repos.token;

import com.example.app.models.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<RefreshToken, UUID> {

        Optional<RefreshToken> findByTokenHash(String tokenHash);
        void deleteAllByUserId(UUID userId);
}