package com.example.app.services;

import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.input.RegisterRequest;
import com.example.app.dto.auth.login.input.VerifyOTPRequest;

import com.example.app.dto.auth.login.output.LoginResponse;
import com.example.app.dto.auth.login.output.RegisterResponse;

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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public RegisterResponse register(
            RegisterRequest request
    ) {

        if (userRepository.existsByUsername(
                request.username()
        )) {

            throw new UserAlreadyExistsException(
                    "Username already exists"
            );
        }


        if (userRepository.existsByEmail(
                request.email()
        )) {

            throw new UserAlreadyExistsException(
                    "Email already exists"
            );
        }


        User user = new User();

        user.setUsername(
                request.username()
        );

        user.setEmail(
                request.email()
        );

        user.setPassword(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setVerified(false);
        
        userRepository.save(user);

        String otp = otpService.generateOTP(
                user.getUsername()
        );

        log.info(
                "OTP generated for user {}: {}",
                user.getUsername(),
                otp
        );


        return new RegisterResponse(
                "Registration was successfully completed. Verify your account.",
                user.getUsername(),
                otp
        );
    }


    @Transactional
    public void verifyUser(
            VerifyOTPRequest request
    ) {

        User user = userRepository
                .findByUsername(
                        request.username()
                )
                .orElseThrow(
                        () -> new UserNotFoundException(
                                "User not found"
                        )
                );


        boolean verified =
                otpService.verifyOTP(
                        user.getEmail(),
                        request.OTPCode()
                );


        if (!verified) {

            throw new IllegalArgumentException(
                    "Invalid or expired OTP"
            );
        }


        user.setVerified(true);

        userRepository.save(user);
    }


    public LoginResponse login(
            LoginRequest request
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );


        User user = userRepository
                .findByUsername(
                        request.username()
                )
                .orElseThrow(
                        () -> new UserNotFoundException("User not found")
                );


        if (!user.isVerified()) {
            throw new UserNotVerifiedException(
                    "User must be verified before login"
            );
        }


        String newAccessToken = jwtService.generateAccessToken(user);


        String newRefreshToken = jwtService.generateRefreshToken(user);

        accessTokenService.saveAccessToken(newAccessToken, user);

        saveRefreshToken(newRefreshToken, user);


        return new LoginResponse(
                newAccessToken,
                newRefreshToken
        );
    }


    private void saveRefreshToken(String refreshToken, User user) {
        String tokenHash = jwtService.hashToken(refreshToken);

        RefreshToken refreshTokenEntity =new RefreshToken();

        refreshTokenEntity.setRefreshTokenHash(tokenHash);

        refreshTokenEntity.setUser(user);

        refreshTokenEntity.setLoggedOut(false);


        tokenRepository.save(refreshTokenEntity);
    }
}