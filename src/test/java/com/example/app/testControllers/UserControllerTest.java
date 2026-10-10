
package com.example.app.testControllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.app.controllers.UserController;
import com.example.app.dto.auth.login.input.LoginRequest;
import com.example.app.dto.auth.login.input.RegisterRequest;
import com.example.app.dto.auth.login.input.VerifyRequest;
import com.example.app.dto.auth.login.output.LoginResponse;
import com.example.app.dto.auth.login.output.RegisterResponse;
import com.example.app.dto.auth.login.output.VerifyResponse;
import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.UserInfoResponse;
import com.example.app.dto.user.output.UserUpdateResponse;
import com.example.app.services.AuthenticationService;
import com.example.app.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private Authentication authentication;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID userId;

    private String username;
    private String email;
    private String password;
    private String otpCode;

    private String accessToken;
    private String refreshToken;

    private String firstname;
    private String lastname;
    private String phoneNumber;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(userController)
            .build();

        objectMapper = new ObjectMapper();

        userId = UUID.randomUUID();

        username = "yeaoke";
        email = "test.example@gmail.com";
        password = "Strongpass1!";
        otpCode = "012302";

        accessToken = UUID.randomUUID().toString();
        refreshToken = UUID.randomUUID().toString();

        firstname = "Nikita";
        lastname = "Stark";
        phoneNumber = "+79991234567";
    }

    @Test
    void register_true() throws Exception {
        RegisterRequest request = new RegisterRequest(
            username, email, password
        );

        RegisterResponse response = new RegisterResponse(
            "Hello", username, otpCode
        );

        when(authenticationService.register(any(RegisterRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(content().json(
                objectMapper.writeValueAsString(response)
            ));

        verify(authenticationService)
            .register(any(RegisterRequest.class));
    }

    @Test
    void verify_true() throws Exception {
        VerifyRequest request = new VerifyRequest(
            username, otpCode
        );

        VerifyResponse response = new VerifyResponse("Verified");

        when(authenticationService.verifyUser(any(VerifyRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/register/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().json(
                objectMapper.writeValueAsString(response)
            ));

        verify(authenticationService)
            .verifyUser(any(VerifyRequest.class));
    }

    @Test
    void login_true() throws Exception {
        LoginRequest request = new LoginRequest(
            username, password
        );

        LoginResponse response = new LoginResponse(
            accessToken, refreshToken
        );

        when(authenticationService.login(any(LoginRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().json(
                objectMapper.writeValueAsString(response)
            ));

        verify(authenticationService)
            .login(any(LoginRequest.class));
    }

    @Test
    void getProfile_true() throws Exception {
        when(authentication.getName())
            .thenReturn(userId.toString());

        UserInfoResponse response = new UserInfoResponse(
            userId,
            firstname,
            lastname,
            username,
            email,
            phoneNumber
        );

        when(userService.getUserProfile(userId))
            .thenReturn(response);

        mockMvc.perform(
                get("/api/auth/me")
                    .principal(authentication)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.firstname").value(firstname))
            .andExpect(jsonPath("$.lastname").value(lastname))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.phoneNumber").value(phoneNumber));

        verify(userService).getUserProfile(userId);
    }

    @Test
    void updateProfileWithNull_true() throws Exception {
        when(authentication.getName())
            .thenReturn(userId.toString());

        UserInfoRequest request = new UserInfoRequest(
            firstname, null, phoneNumber
        );

        UserUpdateResponse response = new UserUpdateResponse(
            "User profile updated successfully"
        );

        when(userService.updateUserDetails(
                eq(userId),
                any(UserInfoRequest.class)
            ))
            .thenReturn(response);

        mockMvc.perform(
                put("/api/auth/me/update")
                    .principal(authentication)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().json(
                objectMapper.writeValueAsString(response)
            ));

        verify(userService).updateUserDetails(
            eq(userId),
            any(UserInfoRequest.class)
        );
    }

    @Test
    void updateProfile_true() throws Exception {
        when(authentication.getName())
            .thenReturn(userId.toString());

        UserInfoRequest request = new UserInfoRequest(
            firstname, lastname, phoneNumber
        );

        UserUpdateResponse response = new UserUpdateResponse(
            "User profile updated successfully"
        );

        when(userService.updateUserDetails(
                eq(userId),
                any(UserInfoRequest.class)
            ))
            .thenReturn(response);

        mockMvc.perform(
                put("/api/auth/me/update")
                    .principal(authentication)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().json(
                objectMapper.writeValueAsString(response)
            ));

        verify(userService).updateUserDetails(
            eq(userId),
            any(UserInfoRequest.class)
        );
    }
}
