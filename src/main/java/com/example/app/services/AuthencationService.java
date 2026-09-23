package com.example.app.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.input.RegisterRequest;
import com.example.app.dto.auth.login.output.LoginResponse;
import com.example.app.models.main.User;
import com.example.app.models.token.Token;
import com.example.app.repos.main.UserRepository;
import com.example.app.repos.token.TokenRepository;
import com.example.app.security.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthencationService {

    //private final Long TTLinMin = 10L;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserRepository userRepository;
    
    private final AuthenticationManager authenticationManager;

    //private final TokenCacheService tokenCacheService;

    private final TokenRepository tokenRepository;

    public void register(RegisterRequest register) {
        User user = new User();

        user.setUsername(register.username());
        user.setEmail(register.email());
        user.setPassword(passwordEncoder.encode(register.password()));

        log.info(
            "Creating new user with id - {}, username - {}, email - {}",
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );

        user = userRepository.save(user);

        log.info("user created. Id - {}", user.getId());
    }

    public LoginResponse login(LoginRequest login) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                login.username(),
                login.password())
        );

        User user = userRepository.findByUsername(login.username())
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with username - " + login.username() + "try register one more time"
            ));
        
        String accessToken = jwtService.generateAccessToken(user);
        
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, refreshToken, user);

        return new LoginResponse(accessToken, refreshToken);
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

        //String userId = user.getId().toString();
        //
        //tokenCacheService.saveCachedAccessRefreshTokens(
        //    accessToken,
        //    refreshToken,
        //    userId,
        //    TTLinMin
        //);

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setUserId(user.getId());
        token.setLoggedOut(false);

        tokenRepository.save(token);
    }

    public ResponseEntity<LoginResponse> refreshToken(
        HttpServletRequest request
    ) {
        
        String authHeader = request.getHeader("Authorization");
        
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
        
            return new ResponseEntity<>(new LoginResponse(accessToken, refreshToken), HttpStatus.OK);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
