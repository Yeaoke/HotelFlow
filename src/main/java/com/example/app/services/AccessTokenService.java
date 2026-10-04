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

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    public void saveAccessToken(String token, User user) {

        String key = PREFIX + token;

        redisTemplate.opsForValue().set(
                key,
                user.getId().toString(),
                ACCESS_TOKEN_TTL
        );
    }

    public boolean isAccessTokenValid(String token) {

        String key = PREFIX + token;

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key)
        );
    }

    public void revokeAccessToken(String token) {
        String key = PREFIX + token;

        redisTemplate.delete(key);
    }
}