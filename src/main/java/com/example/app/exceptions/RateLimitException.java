package com.example.app.exceptions;
public class RateLimitException extends RuntimeException {
    public RateLimitException() {
        super("Too many attempts. Try again later.");
    }
}
