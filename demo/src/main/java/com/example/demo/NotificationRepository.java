package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByEventId(String eventId);
    List<Notification> findByUserId(String userId);
    List<Notification> findByAudienceScope(String audienceScope);
    List<Notification> findByAudienceScopeAndAudienceRole(String audienceScope, String audienceRole);
    List<Notification> findByAudienceScopeAndEventId(String audienceScope, String eventId);
}
