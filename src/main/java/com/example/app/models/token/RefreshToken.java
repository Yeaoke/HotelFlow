package com.example.app.models.token;

import com.example.app.models.main.User;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {

        @Id
        @GeneratedValue
        private UUID id;

        @Column(name = "token_hash", nullable = false, unique = true)
        private String tokenHash;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(name = "logged_out", nullable = false)
        private boolean loggedOut = false;
}