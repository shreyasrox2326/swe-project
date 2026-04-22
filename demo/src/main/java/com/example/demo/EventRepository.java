package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByOrganizerId(String organizerId);
    List<Event> findByStatusIgnoreCase(String status);
}
