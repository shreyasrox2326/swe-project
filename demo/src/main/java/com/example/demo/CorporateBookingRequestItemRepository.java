package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorporateBookingRequestItemRepository extends JpaRepository<CorporateBookingRequestItem, String> {
    List<CorporateBookingRequestItem> findByRequestId(String requestId);
}
