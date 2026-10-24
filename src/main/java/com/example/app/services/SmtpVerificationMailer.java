package com.example.app.services;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.app.exceptions.DeliveryException;
import com.example.app.repos.main.VerificationMailerRepository;
@Service
public class SmtpVerificationMailer implements VerificationMailerRepository {
    
    private final JavaMailSender sender;
    private final String from;
    
    public SmtpVerificationMailer(JavaMailSender sender, @Value("${app.mail.from}") String from) {
        this.sender=sender; this.from=from;
    }
    
    @Override public void send(
        String email,
        String code
    ) {
        var message=new SimpleMailMessage();

        message.setFrom(from); message.setTo(email);

        message.setSubject("HotelFlow: verification code");

        message.setText("Your HotelFlow verification code: " + code + "\nThe code expires in 5 minutes.");
        try {
            sender.send(message);
        } catch (org.springframework.mail.MailException e) {
            throw new DeliveryException();
        }
    }
}
