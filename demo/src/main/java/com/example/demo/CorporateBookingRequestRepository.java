package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorporateBookingRequestRepository extends JpaRepository<CorporateBookingRequest, String> {
    List<CorporateBookingRequest> findByCorporateUserIdOrderByCreatedAtDesc(String corporateUserId);
    List<CorporateBookingRequest> findByOrganizerUserIdOrderByCreatedAtDesc(String organizerUserId);
    List<CorporateBookingRequest> findByEventIdOrderByCreatedAtDesc(String eventId);
}
