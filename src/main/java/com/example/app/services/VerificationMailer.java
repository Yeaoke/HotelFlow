package com.example.app.services;

public interface VerificationMailer {
    void send(String email,String code);
}
