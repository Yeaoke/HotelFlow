package com.example.app.security.jwt;

import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.app.models.main.User;
import com.example.app.repos.token.TokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class JwtService {
    
    @Value("${security.jwt.secret_key}")
    private String secretKey;

    @Value("${security.jwt.access_token_expiration}")
    private Long accessTokenExpiration;

    @Value("${security.jwt.refresh_token_expiration}")
    private Long refreshTokenExpiration;

    private TokenRepository tokenRepository;

    private TokenRedisService tokenRedisService;

    public JwtService(
        TokenRepository tokenRepository,
        TokenRedisService tokenRedisService
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenRedisService = tokenRedisService;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(User user, long expiryTime, String tokenType) {
        JwtBuilder builder = Jwts.builder()
            .subject(user.getUsername())
            .claim("type", tokenType)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiryTime))
            .signWith(getSigningKey());
        
        return builder.compact();
    }

    public String generateAccessToken(User user) {
        String accessToken = generateToken(
            user, 
            accessTokenExpiration, 
            "access"
        );
        tokenRedisService.saveCacheAccessToken(
            accessToken, 
            user.getId().toString(), 
            Duration.ofMinutes(15)
        );
        return accessToken;
    }

    public String generateRefreshToken(User user) {
        String refreshToken = generateToken(
            user, 
            refreshTokenExpiration, 
            "refresh"
        );
        tokenRedisService.saveCacheRefreshToken(
            refreshToken, 
            user.getId().toString(),
            Duration.ofDays(30)
        );
        return refreshToken;
    }

    public Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parser();

        parser.verifyWith(getSigningKey());

        return parser.build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenValidByExpiration(String token) {
        return !extractExpiration(token).before(new Date());
    }

    public boolean isValid(String token, User user) {

        String username = extractUsername(token);

        String tokenType = extractClaim(token, claims -> claims.get("type", String.class));

        boolean isValidToken = tokenRepository
                .findByAccessToken(token)
                .map(storedToken -> !storedToken.isLoggedOut())
                .orElse(false);

        return username.equals(user.getUsername())
                && "access".equals(tokenType)
                && isTokenValidByExpiration(token)
                && isValidToken;
    }

    public boolean isValidRefresh(String token, User user) {
        
        String username = extractUsername(token);

        String tokenType = extractClaim(token, claims -> claims.get("type", String.class));

        boolean isValidRefreshToken = tokenRepository.findByRefreshToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername())
                && "refresh".equals(tokenType)
                && isTokenValidByExpiration(token)
                && isValidRefreshToken;
    }
}
