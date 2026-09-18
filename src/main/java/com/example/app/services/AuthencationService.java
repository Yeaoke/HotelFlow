package com.example.app.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.output.AuthResponse;
import com.example.app.models.User;
import com.example.app.repos.UserRepository;
import com.example.app.security.UserRole.UserRole;
import com.example.app.security.jwt.JwtService;
import com.example.app.security.jwt.Token.model.Token;
import com.example.app.security.jwt.Token.repo.TokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthencationService {
    
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    public void register(LoginRequest login) {
        User user = new User();

        user.setUsername(login.username());
        user.setEmail(login.email());
        user.setEmailVerificationTime(LocalDate.now());
        user.setPassword(passwordEncoder.encode(login.password()));
        user.setRole(UserRole.USER);

        log.info("Creating new user with id - {}, username - {}", user.getId(), user.getUsername());

        user = userRepository.save(user);

        log.info("user created. Id - {}", user.getId());
    }

    public void revokeAllTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokens(user.getId());
        
        if (!validTokens.isEmpty()) {
            validTokens.forEach(
                t -> t.setLoggedOut(true)
            );
        }

        tokenRepository.saveAll(validTokens);
    }

    public void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setUser(user);

        tokenRepository.save(token);
    }

    public ResponseEntity<AuthResponse> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        
        String authHeader = request.getHeader("Authorication");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username - " + username));

        if (jwtService.isValidRefresh(token, user)) {

            String accessToken = jwtService.generateAccessToken(user);
            
            String refreshToken = jwtService.generateRefreshToken(user);
            
            revokeAllTokens(user);

            saveUserToken(accessToken, refreshToken, user);
        
            return new ResponseEntity<>(new AuthResponse(accessToken, refreshToken), HttpStatus.OK);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
