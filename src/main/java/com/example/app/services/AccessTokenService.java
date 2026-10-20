package com.example.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import com.example.app.models.main.User;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "access_token:";

    private final com.example.app.security.jwt.JwtService jwtService;

    public void saveAccessToken(String token, User user) {

        String key = PREFIX + jwtService.hashToken(token);

        Duration ttl = Duration.between(java.time.Instant.now(), jwtService.extractExpiration(token).toInstant());
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Access token has expired");
        }

        redisTemplate.opsForValue().set(
                key,
                user.getId().toString(),
                ttl
        );
    }

    public boolean isAccessTokenValid(String token) {

        String key = PREFIX + jwtService.hashToken(token);

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key)
        );
    }

    public void revokeAccessToken(String token) {
        String key = PREFIX + jwtService.hashToken(token);

        redisTemplate.delete(key);
    }
}
