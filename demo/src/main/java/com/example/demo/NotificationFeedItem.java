package com.example.demo;

import java.sql.Timestamp;

public class NotificationFeedItem {
    private String notificationId;
    private String type;
    private String message;
    private Timestamp sentAt;
    private String eventId;
    private String userId;
    private String audienceScope;
    private String audienceRole;
    private String metadata;
    private Timestamp readAt;

    public NotificationFeedItem() {
    }

    public NotificationFeedItem(Notification notification, Timestamp readAt) {
        this.notificationId = notification.getNotificationId();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.sentAt = notification.getSentAt();
        this.eventId = notification.getEventId();
        this.userId = notification.getUserId();
        this.audienceScope = notification.getAudienceScope();
        this.audienceRole = notification.getAudienceRole();
        this.metadata = notification.getMetadata();
        this.readAt = readAt;
    }

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

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAudienceScope() { return audienceScope; }
    public void setAudienceScope(String audienceScope) { this.audienceScope = audienceScope; }

    public String getAudienceRole() { return audienceRole; }
    public void setAudienceRole(String audienceRole) { this.audienceRole = audienceRole; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Timestamp getReadAt() { return readAt; }
    public void setReadAt(Timestamp readAt) { this.readAt = readAt; }
}
