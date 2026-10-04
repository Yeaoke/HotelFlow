package com.example.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    private static final String OTP_PREFIX = "otp:";

    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final Random random = new Random();

    public String generateOTP(String username) {
        String otp = String.format("%06d", random.nextInt(1_000_000));

        saveOTP(username, otp);

        return otp;
    }

    public void saveOTP(String username, String otp) {
        String key = OTP_PREFIX + username;

        redisTemplate.opsForValue().set(key, otp, OTP_TTL);
    } 

    public boolean verifyOTP(String username, String otp) {
        String key = OTP_PREFIX + username;

        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) {
            return false;
        }

        if (!savedOtp.equals(otp)) {
            return false;
        }

        redisTemplate.delete(key);

        return true;
    }

    public void deleteOTP(String username) {
        String key = OTP_PREFIX + username;

        redisTemplate.delete(key);
    }
}