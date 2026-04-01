package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, String> {
    // You can add custom queries here if needed
}
