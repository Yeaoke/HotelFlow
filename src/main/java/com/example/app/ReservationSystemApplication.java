package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//@EnableCaching
public class ReservationSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReservationSystemApplication.class, args);
	}
}
