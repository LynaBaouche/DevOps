package com.etudlife.controller;

import com.etudlife.model.Notification;
import com.etudlife.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // liste des notifs
    @GetMapping("/{userId}")
    public List<Notification> getNotifications(@PathVariable Long userId) {
        return service.getForUser(userId);
    }

    // compteur non lues
    @GetMapping("/{userId}/unread-count")
    public long unreadCount(@PathVariable Long userId) {
        return service.countUnread(userId);
    }

    // marquer comme lu
    @PutMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        service.markAsRead(id);
    }
}
