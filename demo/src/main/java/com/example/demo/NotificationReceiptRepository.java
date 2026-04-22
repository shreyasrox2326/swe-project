package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationReceiptRepository extends JpaRepository<NotificationReceipt, NotificationReceiptId> {
    Optional<NotificationReceipt> findByNotificationIdAndUserId(String notificationId, String userId);
    List<NotificationReceipt> findByUserId(String userId);
    List<NotificationReceipt> findByUserIdAndNotificationIdIn(String userId, List<String> notificationIds);
}
