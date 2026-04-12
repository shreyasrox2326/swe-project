package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "staff_event_assignments")
public class StaffEventAssignment {

    @Id
    @Column(name = "assignment_id", nullable = false)
    private String assignmentId;

    @Column(name = "staff_user_id", nullable = false)
    private String staffUserId;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "assigned_by_user_id")
    private String assignedByUserId;

    @Column(name = "assigned_at", nullable = false)
    private Timestamp assignedAt = new Timestamp(System.currentTimeMillis());

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getStaffUserId() { return staffUserId; }
    public void setStaffUserId(String staffUserId) { this.staffUserId = staffUserId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(String assignedByUserId) { this.assignedByUserId = assignedByUserId; }

    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }
}
