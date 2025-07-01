package com.ghost.invenarc.repository;

import com.ghost.invenarc.model.Notification;
import com.ghost.invenarc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a specific user
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Find unread notifications for a user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Count unread notifications for a user
    long countByUserAndIsReadFalse(User user);
    
    // Find notifications by type
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, String type);
    
    // Find recent notifications (last 30 days)
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :date ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("user") User user, @Param("date") java.time.LocalDateTime date);
} 