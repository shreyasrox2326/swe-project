package com.example.demo;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final NotificationReceiptRepository receiptRepo;
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final EventRepository eventRepo;
    private final StaffEventAssignmentRepository staffAssignmentRepo;
    private final CorporateBookingRequestRepository corporateRequestRepo;

    public NotificationController(
            NotificationRepository notificationRepo,
            NotificationReceiptRepository receiptRepo,
            UserRepository userRepo,
            BookingRepository bookingRepo,
            EventRepository eventRepo,
            StaffEventAssignmentRepository staffAssignmentRepo,
            CorporateBookingRequestRepository corporateRequestRepo
    ) {
        this.notificationRepo = notificationRepo;
        this.receiptRepo = receiptRepo;
        this.userRepo = userRepo;
        this.bookingRepo = bookingRepo;
        this.eventRepo = eventRepo;
        this.staffAssignmentRepo = staffAssignmentRepo;
        this.corporateRequestRepo = corporateRequestRepo;
    }

    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        if (notification.getSentAt() == null) {
            notification.setSentAt(new Timestamp(System.currentTimeMillis()));
        }
        if (notification.getAudienceScope() == null || notification.getAudienceScope().isBlank()) {
            notification.setAudienceScope(notification.getUserId() == null ? "GLOBAL" : "DIRECT");
        }
        if (notification.getMetadata() == null || notification.getMetadata().isBlank()) {
            notification.setMetadata("{}");
        }

        Notification created = notificationRepo.save(notification);
        if ("DIRECT".equalsIgnoreCase(created.getAudienceScope()) && created.getUserId() != null) {
            ensureReceipt(created.getNotificationId(), created.getUserId(), null);
        }
        return created;
    }

    @GetMapping
    public List<Notification> getAll() {
        return notificationRepo.findAll();
    }

    @GetMapping("/event/{eventId}")
    public List<Notification> getByEvent(@PathVariable String eventId) {
        List<Notification> direct = notificationRepo.findByEventId(eventId);
        List<Notification> eventScoped = notificationRepo.findByAudienceScopeAndEventId("EVENT", eventId);
        Map<String, Notification> merged = new LinkedHashMap<>();
        direct.forEach(notification -> merged.put(notification.getNotificationId(), notification));
        eventScoped.forEach(notification -> merged.put(notification.getNotificationId(), notification));
        return new ArrayList<>(merged.values());
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getDirectByUser(@PathVariable String userId) {
        return notificationRepo.findByUserId(userId);
    }

    @GetMapping("/visible/{userId}")
    public List<NotificationFeedItem> getVisibleNotifications(@PathVariable String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Notification> visible = new LinkedHashMap<>();

        notificationRepo.findByUserId(userId).forEach(notification -> visible.put(notification.getNotificationId(), notification));
        notificationRepo.findByAudienceScope("GLOBAL").forEach(notification -> visible.put(notification.getNotificationId(), notification));
        notificationRepo.findByAudienceScopeAndAudienceRole("ROLE", user.getType().name()).forEach(notification -> visible.put(notification.getNotificationId(), notification));

        Set<String> relevantEventIds = resolveRelevantEventIds(user);
        if (!relevantEventIds.isEmpty()) {
            notificationRepo.findByAudienceScopeAndEventIdIn("EVENT", new ArrayList<>(relevantEventIds))
                    .forEach(notification -> visible.put(notification.getNotificationId(), notification));
        }

        List<String> notificationIds = new ArrayList<>(visible.keySet());
        Map<String, NotificationReceipt> receiptsByNotificationId = new HashMap<>();
        if (!notificationIds.isEmpty()) {
            receiptRepo.findByUserIdAndNotificationIdIn(userId, notificationIds)
                    .forEach(receipt -> receiptsByNotificationId.put(receipt.getNotificationId(), receipt));
        }

        List<NotificationFeedItem> feed = new ArrayList<>();
        for (Notification notification : visible.values()) {
            NotificationReceipt receipt = receiptsByNotificationId.get(notification.getNotificationId());
            Timestamp readAt = receipt != null
                    ? receipt.getReadAt()
                    : notification.getUserId() != null && notification.getUserId().equals(userId) ? notification.getReadAt() : null;
            feed.add(new NotificationFeedItem(notification, readAt));
        }
        feed.sort(Comparator.comparing(NotificationFeedItem::getSentAt, Comparator.nullsLast(Timestamp::compareTo)).reversed());
        return feed;
    }

    @GetMapping("/visible/{userId}/count")
    public Map<String, Integer> getVisibleNotificationCount(@PathVariable String userId) {
        List<NotificationFeedItem> visibleNotifications = getVisibleNotifications(userId);
        int unread = (int) visibleNotifications.stream().filter(item -> item.getReadAt() == null).count();
        return Map.of("total", visibleNotifications.size(), "unread", unread);
    }

    @PatchMapping("/{notificationId}/read")
    @Transactional
    public NotificationFeedItem markAsRead(@PathVariable String notificationId, @RequestParam String userId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        Timestamp readAt = new Timestamp(System.currentTimeMillis());

        if (notification.getUserId() != null && notification.getUserId().equals(userId)) {
            notification.setReadAt(readAt);
            notificationRepo.save(notification);
        }

        ensureReceipt(notificationId, userId, readAt);
        return new NotificationFeedItem(notification, readAt);
    }

    @PatchMapping("/visible/{userId}/read-all")
    @Transactional
    public List<NotificationFeedItem> markAllAsRead(@PathVariable String userId) {
        Timestamp readAt = new Timestamp(System.currentTimeMillis());
        List<NotificationFeedItem> visibleNotifications = getVisibleNotifications(userId);

        List<String> notificationIds = visibleNotifications.stream()
                .map(NotificationFeedItem::getNotificationId)
                .toList();
        Map<String, Notification> notificationsById = new HashMap<>();
        notificationRepo.findAllById(notificationIds)
                .forEach(notification -> notificationsById.put(notification.getNotificationId(), notification));

        List<NotificationReceipt> existingReceipts = receiptRepo.findByUserIdAndNotificationIdIn(userId, notificationIds);
        Map<String, NotificationReceipt> receiptsByNotificationId = new HashMap<>();
        existingReceipts.forEach(receipt -> receiptsByNotificationId.put(receipt.getNotificationId(), receipt));

        List<Notification> directNotificationsToSave = new ArrayList<>();
        List<NotificationReceipt> receiptsToSave = new ArrayList<>();
        for (NotificationFeedItem item : visibleNotifications) {
            Notification notification = notificationsById.get(item.getNotificationId());
            if (notification == null) {
                continue;
            }
            if (notification.getUserId() != null && notification.getUserId().equals(userId)) {
                notification.setReadAt(readAt);
                directNotificationsToSave.add(notification);
            }
            NotificationReceipt receipt = receiptsByNotificationId.getOrDefault(notification.getNotificationId(), new NotificationReceipt());
            receipt.setNotificationId(notification.getNotificationId());
            receipt.setUserId(userId);
            if (receipt.getDeliveredAt() == null) {
                receipt.setDeliveredAt(readAt);
            }
            receipt.setReadAt(readAt);
            receiptsToSave.add(receipt);
            item.setReadAt(readAt);
        }
        if (!directNotificationsToSave.isEmpty()) {
            notificationRepo.saveAll(directNotificationsToSave);
        }
        if (!receiptsToSave.isEmpty()) {
            receiptRepo.saveAll(receiptsToSave);
        }

        return visibleNotifications;
    }

    private Set<String> resolveRelevantEventIds(User user) {
        Set<String> eventIds = new HashSet<>();

        switch (user.getType()) {
            case customer -> bookingRepo.findByUserId(user.getUser_id())
                    .forEach(booking -> eventIds.add(booking.getEventId()));
            case organizer -> eventRepo.findByOrganizerId(user.getUser_id())
                    .forEach(event -> eventIds.add(event.getEventId()));
            case staff -> staffAssignmentRepo.findByStaffUserId(user.getUser_id())
                    .forEach(assignment -> eventIds.add(assignment.getEventId()));
            case corporate -> corporateRequestRepo.findByCorporateUserIdOrderByCreatedAtDesc(user.getUser_id())
                    .forEach(request -> eventIds.add(request.getEventId()));
            case admin -> {
            }
        }

        return eventIds;
    }

    private void ensureReceipt(String notificationId, String userId, Timestamp readAt) {
        NotificationReceipt receipt = receiptRepo.findByNotificationIdAndUserId(notificationId, userId)
                .orElseGet(NotificationReceipt::new);
        receipt.setNotificationId(notificationId);
        receipt.setUserId(userId);
        if (receipt.getDeliveredAt() == null) {
            receipt.setDeliveredAt(new Timestamp(System.currentTimeMillis()));
        }
        if (readAt != null) {
            receipt.setReadAt(readAt);
        }
        receiptRepo.save(receipt);
    }
}
