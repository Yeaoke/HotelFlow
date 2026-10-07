package com.example.app.controllers;

import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.input.RegisterRequest;
import com.example.app.dto.auth.login.input.VerifyOTPRequest;
import com.example.app.dto.auth.login.output.LoginResponse;
import com.example.app.dto.auth.login.output.RegisterResponse;
import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.UserInfoResponse;
import com.example.app.services.AuthenticationService;
import com.example.app.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Регистрация пользователя",
            description = "Регистрирует нового пользователя и отправляет OTP-код"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь уже существует"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @Operation(
            summary = "Подтвердить регистрацию",
            description = "Подтверждает регистрацию пользователя с помощью OTP-кода"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно подтверждён"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный или просроченный OTP-код"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @PostMapping("/register/verify")
    public ResponseEntity<Void> verify(
            @Valid @RequestBody VerifyOTPRequest request
    ) {
        authenticationService.verifyUser(request);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Авторизация",
            description = "Авторизует пользователя и возвращает access и refresh токены"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Авторизация успешна"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный логин или пароль"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authenticationService.login(request)
        );
    }

    @Operation(
            summary = "Получить текущего пользователя",
            description = "Возвращает профиль текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль успешно получен"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        return ResponseEntity.ok(
                userService.getUserProfile(userId)
        );
    }

    @Operation(
            summary = "Обновить профиль",
            description = "Обновляет данные текущего авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль успешно обновлён"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserInfoRequest request
    ) {
        UUID userId = getUserId(authentication);

        userService.updateUserDetails(userId, request);

        return ResponseEntity.ok().build();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}