package com.example.app.repos;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.app.models.User;

public class IUserServiceImpl implements IUserService {

    private final UserRepository userRepository;

    public IUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Not found username - " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Not found username - " + username));
        
        if (user == null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean existsByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElse(null);
    
        if (user == null) {
            return false;
        }

        return true;
    }
}
