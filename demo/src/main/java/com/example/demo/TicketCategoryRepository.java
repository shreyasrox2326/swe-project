package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, String> {
    // You can add custom queries here if needed
}
