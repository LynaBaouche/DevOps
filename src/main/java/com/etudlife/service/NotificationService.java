package com.etudlife.service;

import com.etudlife.model.Notification;
import com.etudlife.model.NotificationType;
import com.etudlife.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public Notification create(Long userId, NotificationType type, String message, String link) {
        Notification n = new Notification(userId, type, message, link);
        return repo.save(n);
    }

    public List<Notification> getForUser(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long id) {
        Notification n = repo.findById(id).orElseThrow();
        n.setRead(true);
        repo.save(n);
    }

    // ✅ ICI AUSSI : IsRead
    public long countUnread(Long userId) {
        return repo.countByUserIdAndIsReadFalse(userId);
    }
}
