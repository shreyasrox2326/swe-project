package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationRepository notificationRepo;

    public NotificationController(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    // CREATE notification
    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        if (notification.getSentAt() == null) {
            notification.setSentAt(new Timestamp(System.currentTimeMillis()));
        }
        return notificationRepo.save(notification);
    }

    // GET all notifications
    @GetMapping
    public List<Notification> getAll() {
        return notificationRepo.findAll();
    }

    // GET notifications by event
    @GetMapping("/event/{eventId}")
    public List<Notification> getByEvent(@PathVariable String eventId) {
        return notificationRepo.findByEventId(eventId);
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(@PathVariable String userId) {
        return notificationRepo.findByUserId(userId);
    }

    @PatchMapping("/{notificationId}/read")
    public Notification markAsRead(@PathVariable String notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setReadAt(new Timestamp(System.currentTimeMillis()));
        return notificationRepo.save(notification);
    }

    @PatchMapping("/user/{userId}/read-all")
    public List<Notification> markAllAsRead(@PathVariable String userId) {
        List<Notification> notifications = notificationRepo.findByUserId(userId);
        Timestamp readAt = new Timestamp(System.currentTimeMillis());
        notifications.forEach(notification -> notification.setReadAt(readAt));
        return notificationRepo.saveAll(notifications);
    }
}
