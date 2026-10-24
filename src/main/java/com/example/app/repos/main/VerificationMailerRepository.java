package com.example.app.repos.main;

public interface VerificationMailerRepository {
    void send(String email,String code);
}
