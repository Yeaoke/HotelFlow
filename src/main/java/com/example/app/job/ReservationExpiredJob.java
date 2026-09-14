package com.example.app.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.app.services.ReservationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@RequiredArgsConstructor
public class ReservationExpiredJob {

    private final ReservationService reservationService;

    @Scheduled(cron = "0 0 2 * * *")
    public void expireReservations() {
        long expiredReservations = reservationService.expireReservations();
    
        log.info("Expired reservations {}", expiredReservations);
    }
}