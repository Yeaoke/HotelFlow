package com.example.app.security.jwt;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service 
public class TokenCacheService {
    
    private final StringRedisTemplate redisTemplate;


    public TokenCacheService(StringRedisTemplate template) {
        this.redisTemplate = template;
    } 

    public void saveCachedAccessRefreshTokens(
        String accessToken,
        String refreshToken,
        String userId,
        Long lifetime
    ) {
        String key = "session: " + accessToken + " " + refreshToken;

        redisTemplate.opsForValue().set(key, userId, lifetime);
    }

    public String getCacheTokens(
        String accessToken,
        String refreshToken
    ) {
        String key = "session: " + accessToken + " " + refreshToken;

        return redisTemplate.opsForValue().get(key);
    }

    public void deleteCacheTokens(
        String accessToken,
        String refreshToken
    ) {
        redisTemplate.delete("session: " + accessToken + " " + refreshToken);
    }

    public void saveCacheAccessToken(
        String accessToken, 
        String userId, 
        Long lifetime
    ) {
        String key = "session: " + accessToken;

        redisTemplate.opsForValue().set(key, userId, lifetime);
    }   

    public String getCacheAccessToken(
        String accessToken
    ) {
        String key = "session: " + accessToken;

        return redisTemplate.opsForValue().get(key);
    }

    public void deleteCacheAccessToken(
        String accessToken
    ) {
        redisTemplate.delete("session: " + accessToken);
    }

    public void saveCacheRefreshToken(
        String refreshToken, 
        String userId, 
        Long lifetime
    ) {
        String key = "session: " + refreshToken;

        redisTemplate.opsForValue().set(key, userId, lifetime);
    } 
    
    public String getCacheRefreshToken(
        String refreshToken
    ) {
        String key = "session: " + refreshToken;

        return redisTemplate.opsForValue().get(key);
    }

    public void deleteCacheRefreshToken(
        String refreshToken
    ) {
        redisTemplate.delete("session: " + refreshToken);
    }
}
