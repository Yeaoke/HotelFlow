package com.example.app.services;
import com.example.app.dto.auth.login.input.*;
import com.example.app.dto.auth.login.output.*;
import com.example.app.exceptions.*;
import com.example.app.models.main.User;
import com.example.app.models.token.RefreshToken;
import com.example.app.repos.main.UserRepository;
import com.example.app.repos.token.TokenRepository;
import com.example.app.security.jwt.JwtService;
import com.example.app.security.UserRole.UserRole;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.*;
@Service @RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final AuthenticationManager manager;
    private final JwtService jwt;
    private final AccessTokenService access;
    private final TokenRepository tokens;
    private final OtpService otp;
    @Transactional
    public RegisterResponse register(RegisterRequest request,String ip) {
        String email=request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByUsername(request.username()) || users.existsByEmail(email))
            throw new UserAlreadyExistsException("Username or email already registered");
        User user=new User();
        user.setUsername(request.username()); user.setEmail(email);
        if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Password must not exceed 72 UTF-8 bytes");
        user.setPassword(passwords.encode(request.password())); user.setVerified(false); user.setRole(UserRole.USER);
        users.saveAndFlush(user);
        otp.issue(user.getId(),user.getEmail(),ip);
        return new RegisterResponse("Check your email to verify your account",user.getUsername());
    }
    @Transactional
    public VerifyResponse verifyUser(VerifyRequest request) {
        User user=users.findByUsernameForUpdate(request.username())
            .orElseThrow(()->new IllegalArgumentException("Invalid or expired verification code"));
        if (user.isVerified()) throw new ConflictException("Account already verified");
        if (!otp.verify(user.getId(),request.OTPCode())) throw new IllegalArgumentException("Invalid or expired verification code");
        user.setVerified(true);
        return new VerifyResponse("Verified");
    }
    @Transactional
    public void resend(ResendRequest request,String ip) {
        // Same HTTP response for unknown and verified users.
        users.findByUsernameForUpdate(request.username()).filter(user->!user.isVerified())
            .ifPresent(user->otp.issue(user.getId(),user.getEmail(),ip));
    }
    @Transactional
    public LoginResponse login(LoginRequest request) {
        manager.authenticate(new UsernamePasswordAuthenticationToken(request.username(),request.password()));
        User user=users.findByUsername(request.username()).orElseThrow(()->new BadCredentialsException("Invalid credentials"));
        if (!user.isVerified()) throw new org.springframework.security.access.AccessDeniedException("Verify your account before login");
        RefreshToken session=new RefreshToken();
        session.setId(UUID.randomUUID()); session.setUser(user); session.setExpiresAt(jwt.sessionExpiration());
        return issuePair(session);
    }
    @Transactional
    public LoginResponse refresh(String token) {
        try {
            var claims=jwt.parse(token,"refresh");
            var session=tokens.findByIdForUpdate(UUID.fromString(claims.get("sid",String.class)))
                .orElseThrow(()->new BadCredentialsException("Invalid refresh token"));
            if (session.isLoggedOut() || !session.getExpiresAt().isAfter(Instant.now())
                    || !jwt.hashToken(token).equals(session.getTokenHash())
                    || !session.getUser().getId().toString().equals(claims.getSubject())
                    || !session.getUser().isVerified())
                throw new BadCredentialsException("Invalid refresh token");
            return issuePair(session);
        } catch (JwtException | IllegalArgumentException e) { throw new BadCredentialsException("Invalid refresh token"); }
    }
    private LoginResponse issuePair(RefreshToken session) {
        String previous=session.getAccessHash();
        String refresh=jwt.generateRefreshToken(session.getUser(),session.getId(),session.getExpiresAt());
        String token=jwt.generateAccessToken(session.getUser(),session.getId(),session.getExpiresAt());
        session.setTokenHash(jwt.hashToken(refresh)); session.setAccessHash(jwt.hashToken(token));
        tokens.saveAndFlush(session);
        access.saveAccessToken(token,session.getId());
        access.revokeHash(previous);
        return new LoginResponse(token,refresh);
    }
    @Transactional
    public void logout(String token,UUID userId) {
        var claims=jwt.parse(token,"access");
        var session=tokens.findByIdForUpdate(UUID.fromString(claims.get("sid",String.class)))
            .orElseThrow(()->new BadCredentialsException("Invalid session"));
        if (!userId.equals(session.getUser().getId())) throw new BadCredentialsException("Invalid session");
        session.setLoggedOut(true);
        tokens.saveAndFlush(session);
        access.revokeHash(session.getAccessHash());
    }
}
