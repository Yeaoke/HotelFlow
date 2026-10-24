package com.example.app.security.jwt;
import com.example.app.models.main.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
@Service
public class JwtService {
    private final SecretKey key;
    private final long accessMillis;
    private final long refreshMillis;
    public JwtService(
        @Value("${security.jwt.secret_key}") String secret,
        @Value("${security.jwt.access_token_expiration:900000}") long accessMillis,
        @Value("${security.jwt.refresh_token_expiration:604800000}") long refreshMillis
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        if (accessMillis <= 0 || refreshMillis < accessMillis) {
            throw new IllegalArgumentException("Invalid token lifetimes");
        }

        this.accessMillis = accessMillis;
        this.refreshMillis = refreshMillis;
    }
    
    public Instant sessionExpiration() {
        return Instant.now().plusMillis(refreshMillis);
    }
    
    public String generateAccessToken(User user,
        UUID sessionId,
        Instant sessionExpires
    ) {
        Instant expires=Instant.now().plusMillis(accessMillis);

        return generate(user,sessionId,"access",expires.isBefore(sessionExpires) ? expires : sessionExpires);
    }
    public String generateRefreshToken(
        User user,
        UUID sessionId,
        Instant expires
    ) {
        return generate(user,sessionId,"refresh",expires);
    }
    private String generate(User user,
        UUID sid,
        String type,
        Instant expires
    ) {
        return Jwts.builder().issuer("hotelflow").subject(user.getId().toString())
            .id(UUID.randomUUID().toString()).claim("sid",sid.toString()).claim("type",type)
            .issuedAt(new Date()).expiration(Date.from(expires)).signWith(key).compact();
    }

    public Claims parse(
        String token,
        String type
    ) {
        Claims claims = Jwts.parser().verifyWith(key).requireIssuer("hotelflow")
            .require("type",type).build().parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || claims.getId() == null) {
            throw new JwtException("Missing token claims");
        }

        UUID.fromString(claims.getSubject());
        UUID.fromString(claims.get("sid",String.class));

        return claims;
    }

    public <T> T extractClaim(
        String token,
        Function<Claims,T> resolver
    ) {
        return resolver.apply(Jwts.parser().verifyWith(key).requireIssuer("hotelflow").build().parseSignedClaims(token).getPayload());
    }

    public Date extractExpiration(
        String token
    ) {
        return extractClaim(token,Claims::getExpiration);
    }
    public String hashToken(
        String token
    ) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
