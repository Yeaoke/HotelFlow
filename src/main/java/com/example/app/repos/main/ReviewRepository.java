package com.example.app.repos.main;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.app.models.main.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {}
