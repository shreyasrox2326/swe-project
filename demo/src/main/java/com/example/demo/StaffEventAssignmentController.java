package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/staff-assignments")
@CrossOrigin
public class StaffEventAssignmentController {

    private final StaffEventAssignmentRepository assignmentRepo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;

    public StaffEventAssignmentController(
            StaffEventAssignmentRepository assignmentRepo,
            UserRepository userRepo,
            EventRepository eventRepo
    ) {
        this.assignmentRepo = assignmentRepo;
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
    }

    @GetMapping
    public List<StaffEventAssignment> getAll() {
        return assignmentRepo.findAll();
    }

    @GetMapping("/staff/{staffUserId}")
    public List<StaffEventAssignment> getByStaff(@PathVariable String staffUserId) {
        return assignmentRepo.findByStaffUserId(staffUserId);
    }

    @GetMapping("/event/{eventId}")
    public List<StaffEventAssignment> getByEvent(@PathVariable String eventId) {
        return assignmentRepo.findByEventId(eventId);
    }

    @PostMapping
    public StaffEventAssignment create(@RequestBody StaffEventAssignment assignment) {
        User staffUser = userRepo.findById(assignment.getStaffUserId())
                .orElseThrow(() -> new RuntimeException("Staff user not found"));
        if (staffUser.getType() != UserType.staff) {
            throw new RuntimeException("User is not staff");
        }

        eventRepo.findById(assignment.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (assignment.getAssignmentId() == null || assignment.getAssignmentId().isBlank()) {
            assignment.setAssignmentId(UUID.randomUUID().toString());
        }
        if (assignment.getAssignedAt() == null) {
            assignment.setAssignedAt(new Timestamp(System.currentTimeMillis()));
        }
        return assignmentRepo.save(assignment);
    }

    @DeleteMapping("/{assignmentId}")
    public void delete(@PathVariable String assignmentId) {
        assignmentRepo.deleteById(assignmentId);
    }
}
