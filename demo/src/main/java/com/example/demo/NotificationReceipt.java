package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "notification_receipts")
@IdClass(NotificationReceiptId.class)
public class NotificationReceipt {

    @Id
    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "delivered_at", nullable = false)
    private Timestamp deliveredAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "read_at")
    private Timestamp readAt;

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Timestamp getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Timestamp deliveredAt) { this.deliveredAt = deliveredAt; }

    public Timestamp getReadAt() { return readAt; }
    public void setReadAt(Timestamp readAt) { this.readAt = readAt; }
}
