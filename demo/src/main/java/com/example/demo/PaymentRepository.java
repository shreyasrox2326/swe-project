package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByBookingId(String bookingId); // since UNIQUE
    List<Payment> findByBookingIdIn(List<String> bookingIds);
}
