package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.app.repos")
@EntityScan(basePackages = "com.example.app.models")
@EnableCaching
public class ReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationSystemApplication.class, args);
	}
}
