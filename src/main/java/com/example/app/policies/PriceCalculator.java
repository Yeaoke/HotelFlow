package com.example.app.policies;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public final class PriceCalculator {

    private PriceCalculator() {}

    public static long calculate(
        Long nightlyPrice,
        LocalDate start,
        LocalDate end
    ) {
        if (nightlyPrice == null || nightlyPrice < 0) {
            throw new IllegalArgumentException("Invalid room price");
        
        }
        long nights = ChronoUnit.DAYS.between(start,end);
        
        if (nights < 1 || nights > 30) {
            throw new IllegalArgumentException("Invalid stay length");
        }
        
        try {
            return Math.multiplyExact(nightlyPrice,nights);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Reservation price is too large");
        }
    }
}
