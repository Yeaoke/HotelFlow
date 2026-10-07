package com.example.app.repos.main;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.models.main.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {}
