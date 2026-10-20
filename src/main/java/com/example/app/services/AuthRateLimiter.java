package com.example.app.services;
import com.example.app.exceptions.RateLimitException;
import com.example.app.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class AuthRateLimiter {

    private final StringRedisTemplate redis;
    private final JwtService jwt;

    private static final DefaultRedisScript<Long> COUNT = new DefaultRedisScript<>(
        """
        local n=redis.call('INCR',KEYS[1])
        if n == 1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end
        return n
        """,
        Long.class
    );

    public void check(
        String action,
        String ip
    ) {
        boolean delivery = action.equals("register") || action.equals("resend");

        Long n = redis.execute(COUNT,List.of("auth-limit:" + action + ":" + jwt.hashToken(ip)), delivery ? "3600" : "60");

        if (n == null || n > (delivery ? 10 : 30)) {
            throw new RateLimitException();
        }
    }
}
