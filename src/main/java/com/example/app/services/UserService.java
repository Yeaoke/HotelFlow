
package com.example.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.UserInfoResponse;
import com.example.app.dto.user.output.UserUpdateResponse;
import com.example.app.exceptions.UserNotFoundException;
import com.example.app.models.main.User;
import com.example.app.repos.main.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserUpdateResponse updateUserDetails(
            UUID userId,
            UserInfoRequest dto
    ) {
        validateUserId(userId);

        if (dto == null) {
            throw new IllegalArgumentException(
                "User data can't be null"
            );
        }

        User user = findUserOrThrow(userId);

        if (dto.firstname() != null) {
            user.setFirstname(dto.firstname());
        }

        if (dto.lastname() != null) {
            user.setLastname(dto.lastname());
        }

        if (dto.phoneNumber() != null) {
            user.setPhoneNumber(dto.phoneNumber());
        }

        log.info("User updated: id={}", userId);

        return new UserUpdateResponse(
            "User profile updated successfully"
        );
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserProfile(UUID userId) {
        validateUserId(userId);

        User user = findUserOrThrow(userId);

        return toUserInfoResponse(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsersWhoMadeReview() {
        return userRepository.findAllUsersWhoMadeReview();
    }

    @Transactional
    public void deleteUser(UUID id) {
        validateUserId(id);

        User user = findUserOrThrow(id);

        userRepository.delete(user);

        log.info("User deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Username can't be null or blank"
            );
        }

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + username
            ));
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return userRepository.existsByEmail(email);
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with id: " + userId
            ));
    }

    private UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(
            user.getId(),
            user.getFirstname(),
            user.getLastname(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber()
        );
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new UserNotFoundException(
                "User id can't be null"
            );
        }
    }
}
