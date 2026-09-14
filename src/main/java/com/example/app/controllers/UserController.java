package com.example.app.controllers;

import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.UserInfoResponse;
import com.example.app.models.User;
import com.example.app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<User> createUser(
        @RequestBody UserInfoRequest dto
    ) {
        User user = userService.createUser(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Profile("dev")
    @GetMapping
    public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        List<UserInfoResponse> usersResponse = userService.getAllUsers().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(usersResponse);
    }

    @GetMapping("/rooms/{roomId}/owner/{userId}")
    public ResponseEntity<UserInfoResponse> getUser(
            @PathVariable UUID roomId,
            @PathVariable UUID userId) {
        
        Optional<User> user = userService.getUserById(userId);
        
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToResponseDTO(user.get()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserInfoResponse convertToResponseDTO(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getEmailVerificationTime(),
                null,
                user.getPhoneNumber()
        );
    }
}