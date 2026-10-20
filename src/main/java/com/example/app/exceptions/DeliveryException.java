package com.example.app.exceptions;
public class DeliveryException extends RuntimeException {
    public DeliveryException() {
        super("Verification email could not be sent. Try again later.");
    }
}
