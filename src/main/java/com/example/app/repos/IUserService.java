package com.example.app.repos;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
