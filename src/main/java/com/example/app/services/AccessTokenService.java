package com.example.app.services;
import com.example.app.security.jwt.JwtService;
import com.example.app.repos.token.TokenRepository;
import com.example.app.models.main.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import lombok.RequiredArgsConstructor;
import java.time.*;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class AccessTokenService {
    
    private final StringRedisTemplate redis;
    private final JwtService jwt;
    private final TokenRepository tokens;
    
    private static final String PREFIX = "access_token:";
    
    public void saveAccessToken(
        String token,
        UUID sessionId
    ) {
        Duration ttl = Duration.between(Instant.now(),jwt.extractExpiration(token).toInstant());

        if (ttl.isNegative() || ttl.isZero()) {
            throw new BadCredentialsException(
                "Expired token"
            );
        }

        redis.opsForValue().set(PREFIX+jwt.hashToken(token),sessionId.toString(),ttl);
    }

    @Transactional(readOnly=true)
    public User authenticate(
        String token,
        UUID sessionId,
        UUID userId
    ) {
        String hash=jwt.hashToken(token);
        
        if (!sessionId.toString().equals(redis.opsForValue().get(PREFIX+hash))) {
            throw new BadCredentialsException(
                "Invalid session"
            );
        }
        
        var session=tokens.findById(sessionId).orElseThrow(()->new BadCredentialsException("Invalid session"));
        
        if (session.isLoggedOut() || !session.getExpiresAt().isAfter(Instant.now()) || !hash.equals(session.getAccessHash()) || !userId.equals(session.getUser().getId())){ 
            throw new BadCredentialsException(
                "Invalid session"
            );
        }
        
        User user = session.getUser();
        
        if (!user.isVerified() || !user.isEnabled() || !user.isAccountNonLocked()) {
            throw new BadCredentialsException(
                "Invalid account"
            );
        }

        user.getUsername();

        return user;
    }
    public void revokeHash(String hash) {
        if (hash != null) {
            redis.delete(PREFIX+hash);
        }
    }
}
