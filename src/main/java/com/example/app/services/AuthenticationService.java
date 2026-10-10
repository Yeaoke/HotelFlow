package com.example.app.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.input.RegisterRequest;
import com.example.app.dto.auth.login.input.VerifyRequest;
import com.example.app.dto.auth.login.output.LoginResponse;
import com.example.app.dto.auth.login.output.RegisterResponse;
import com.example.app.dto.auth.login.output.VerifyResponse;
import com.example.app.exceptions.UserAlreadyExistsException;
import com.example.app.exceptions.UserNotFoundException;
import com.example.app.exceptions.verifyExceptions.UserNotVerifiedException;
import com.example.app.models.main.User;
import com.example.app.models.token.RefreshToken;
import com.example.app.repos.main.UserRepository;
import com.example.app.repos.token.TokenRepository;
import com.example.app.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccessTokenService accessTokenService;
    private final TokenRepository tokenRepository;
    private final OtpService otpService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Registration data can't be null"
            );
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException(
                    "Username can't be null or blank"
            );
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException(
                    "Email can't be null or blank"
            );
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException(
                    "Password can't be null or blank"
            );
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    "Email already exists"
            );
        }

        User user = new User();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setVerified(false);

        userRepository.save(user);

        String otp = otpService.generateOTP(user.getUsername());

        otpService.saveOTP(
                user.getUsername(),
                otp
        );

        log.info(
                "OTP generated for user: username={}",
                user.getUsername()
        );

        return new RegisterResponse(
                "Registration was successfully completed. Verify your account.",
                user.getUsername(),
                otp
        );
    }

    @Transactional
    public VerifyResponse verifyUser(VerifyRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "OTP verification data can't be null"
            );
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException(
                    "Username can't be null or blank"
            );
        }

        if (request.OTPCode() == null || request.OTPCode().isBlank()) {
            throw new IllegalArgumentException(
                    "OTP code can't be null or blank"
            );
        }

        User user = userRepository.findByUsername(
                request.username()
        ).orElseThrow(
                () -> new UserNotFoundException(
                        "User not found with username: "
                                + request.username()
                )
        );

        if (user.isVerified()) {
            throw new IllegalArgumentException(
                    "User is already verified"
            );
        }

        boolean verified = otpService.verifyOTP(
                user.getUsername(),
                request.OTPCode()
        );

        if (!verified) {
            throw new IllegalArgumentException(
                    "Invalid or expired OTP"
            );
        }

        user.setVerified(true);

        userRepository.save(user);

        log.info(
                "User verified successfully: username={}",
                user.getUsername()
        );

        VerifyResponse response = new VerifyResponse("Verified");

        return response;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Login data can't be null"
            );
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException(
                    "Username can't be null or blank"
            );
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException(
                    "Password can't be null or blank"
            );
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(
                request.username()
        ).orElseThrow(
                () -> new UserNotFoundException(
                        "User not found with username: "
                                + request.username()
                )
        );

        if (!user.isVerified()) {
            throw new UserNotVerifiedException(
                    "User must be verified before login"
            );
        }

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        accessTokenService.saveAccessToken(
                accessToken,
                user
        );

        saveRefreshToken(
                refreshToken,
                user
        );

        log.info(
                "User logged in successfully: userId={}",
                user.getId()
        );

        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }

    private void saveRefreshToken(
            String refreshToken,
            User user
    ) {
        String tokenHash = jwtService.hashToken(
                refreshToken
        );

        RefreshToken refreshTokenEntity = new RefreshToken();

        refreshTokenEntity.setTokenHash(tokenHash);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setLoggedOut(false);

        tokenRepository.save(refreshTokenEntity);
    }
}