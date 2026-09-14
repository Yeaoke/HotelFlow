package com.example.app.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.app.dto.user.input.UserRequest;
import com.example.app.models.User;
import com.example.app.repos.UserRepository;
import com.example.app.security.UserInfo.UserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User updateDetailsOfUser(
        // часть токена jwt - payload
        UserRequest dto
    ) {

        log.info(
                "Updating info about user: email={}, thread={}",
                dto.email(),
                Thread.currentThread().getName()
        );

        User user = new User();

        UserDetails details = new UserDetails();

        details.setName(dto.name());
        details.setEmail(dto.email());
        details.setEmailVerificationTime(LocalDate.now());
        details.setPhoneNumber(dto.phoneNumber());

        UserDetails savedUser = userRepository.save(details);

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
}