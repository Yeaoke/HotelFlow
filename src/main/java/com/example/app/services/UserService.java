package com.example.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.dto.user.output.UserInfoResponse;
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

    public void updateUserDetails(
        UUID userId,
        UserInfoRequest dto
    ) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("User not found with id - " + userId));
        log.info("User with id - {} found for update", userId);
                        
        user.setFirstname(dto.firstname());
        user.setLastname(dto.lastname());
        user.setPhoneNumber(dto.phoneNumber());
        
        userRepository.save(user);
        log.info("User: id - {}, updated", userId);
    }

    public UserInfoResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with id - " + userId));
            
        return new UserInfoResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstname(),
            user.getFirstname(),
            user.getPhoneNumber()
        );
    }

    public Optional<User> getUserById(UUID id) {

        log.info(
                "Getting user: id={}",
                id
        );

        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {

        log.info("Getting all users");

        return userRepository.findAll();
    }

    public List<User> getAllUsersWhoMadeReview() {

        log.info("Getting users who made reviews");

        return userRepository.findAllUsersWhoMadeReview();
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public User getUserByUsername(String username) {
        Optional<User> extractUser = userRepository.findByUsername(username);
        
        if (extractUser.isEmpty()) {
            log.info("Can't find user with username - {}", username);
            throw new RuntimeException();
        }

        return extractUser.get();
    }

    public boolean existsByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Not found username - " + username));
        
        if (user == null) {
            return false;
        }

        return true;
    }

    public boolean existsByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElse(null);
    
        if (user == null) {
            return false;
        }

        return true;
    }
}