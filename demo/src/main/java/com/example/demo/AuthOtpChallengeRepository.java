package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthOtpChallengeRepository extends JpaRepository<AuthOtpChallenge, String> {
    List<AuthOtpChallenge> findByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(String email, String purpose);
}
