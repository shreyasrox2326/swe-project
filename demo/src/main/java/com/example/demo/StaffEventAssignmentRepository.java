package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffEventAssignmentRepository extends JpaRepository<StaffEventAssignment, String> {
    List<StaffEventAssignment> findByStaffUserId(String staffUserId);
    List<StaffEventAssignment> findByEventId(String eventId);
}
