package com.example.app.exceptions.tokenExceptions;

public class InvalidRefreshTokenException extends IllegalArgumentException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
