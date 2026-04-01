package com.example.demo;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @Column(name = "type", nullable = false)
    private String type; // e.g., "info", "warning"

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "sent_at", nullable = false)
    private Timestamp sentAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "event_id", nullable = false)
    private String eventId; // linked event

    // Getters and setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
}