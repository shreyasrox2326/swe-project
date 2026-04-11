package com.example.demo;

import org.springframework.web.bind.annotation.*;
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
}