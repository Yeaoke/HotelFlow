package com.example.app.security;
import java.util.UUID;
import com.example.app.models.main.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
public final class CurrentUser {
    
    private CurrentUser() {}

    public static UUID id(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User user) || user.getId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        return user.getId();
    }
}

