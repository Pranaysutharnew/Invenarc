package com.ghost.invenarc.controller;

import com.ghost.invenarc.config.JwtConfig;
import com.ghost.invenarc.model.Notification;
import com.ghost.invenarc.model.User;
import com.ghost.invenarc.repository.NotificationRepository;
import com.ghost.invenarc.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    public NotificationController(NotificationRepository notificationRepository, 
                                UserRepository userRepository, 
                                JwtConfig jwtConfig) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.jwtConfig = jwtConfig;
    }

    // Get all notifications for the current user
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching notifications: " + e.getMessage());
        }
    }

    // Get unread notifications count
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            long count = notificationRepository.countByUserAndIsReadFalse(user);
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching unread count: " + e.getMessage());
        }
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, 
                                      @RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            Notification notification = notificationRepository.findById(id).orElse(null);
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }

            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("Unauthorized");
            }

            notification.setRead(true);
            notificationRepository.save(notification);
            return ResponseEntity.ok("Notification marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking notification as read: " + e.getMessage());
        }
    }

    // Mark all notifications as read
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
            for (Notification notification : unreadNotifications) {
                notification.setRead(true);
            }
            notificationRepository.saveAll(unreadNotifications);
            return ResponseEntity.ok("All notifications marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking all notifications as read: " + e.getMessage());
        }
    }

    // Delete notification
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, 
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            Notification notification = notificationRepository.findById(id).orElse(null);
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }

            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("Unauthorized");
            }

            notificationRepository.delete(notification);
            return ResponseEntity.ok("Notification deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting notification: " + e.getMessage());
        }
    }

    // Create a test notification (for demo purposes)
    @PostMapping("/test")
    public ResponseEntity<?> createTestNotification(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            Notification notification = new Notification(
                user, 
                "Test Notification", 
                "This is a test notification to verify the system is working.", 
                "info"
            );
            notificationRepository.save(notification);
            return ResponseEntity.ok("Test notification created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating test notification: " + e.getMessage());
        }
    }

    // Helper method to get user from JWT token
    private User getUserFromToken(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            Claims claims = jwtConfig.parseToken(token);
            String email = claims.getSubject();
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }
} 