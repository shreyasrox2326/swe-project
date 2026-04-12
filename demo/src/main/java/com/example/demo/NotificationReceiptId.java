package com.example.demo;

import java.io.Serializable;
import java.util.Objects;

public class NotificationReceiptId implements Serializable {
    private String notificationId;
    private String userId;

    public NotificationReceiptId() {
    }

    public NotificationReceiptId(String notificationId, String userId) {
        this.notificationId = notificationId;
        this.userId = userId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof NotificationReceiptId that)) return false;
        return Objects.equals(notificationId, that.notificationId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, userId);
    }
}
