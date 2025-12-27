package com.etudlife.repository;

import com.etudlife.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // récupérer les notifications d’un utilisateur
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ⚠️ IMPORTANT : isRead (nom du champ Java)
    long countByUserIdAndIsReadFalse(Long userId);
}
