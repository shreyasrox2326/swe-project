package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, String> {
    // Find policy by event ID
    Optional<RefundPolicy> findByEventId(String eventId);
}