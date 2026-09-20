package com.example.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.app.dto.user.input.UserInfoRequest;
import com.example.app.models.User;
import com.example.app.repos.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User updateDetailsOfUser(
        // часть токена jwt - payload
        UserInfoRequest dto
    ) {

        User userDetails = new User();

        userDetails.setName(dto.name());
        userDetails.setPhoneNumber(dto.phoneNumber());

        User savedUser = userRepository.save(userDetails);

        log.info(
                "User updated: id={}"
                //savedUser.getId()
        );

        return savedUser;
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