package com.etudlife.repository;

import java.time.Instant;
import java.util.List;

import com.etudlife.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 1. Récupérer les 50 derniers messages d'une conversation (Historique initial)
    // Dans MessageRepository.java
    @Query(value = "SELECT m FROM Message m WHERE m.conversationId = :id ORDER BY m.timestamp DESC LIMIT 50")
    List<Message> findTop50ByConversationIdOrderByTimestampDesc(@Param("id") Long id);

    // 2. Récupérer les messages après un certain horodatage (Pour le Polling)
    List<Message> findByConversationIdAndTimestampAfterOrderByTimestampAsc(
            Long conversationId, Instant timestamp
    );

    // 3. Récupérer l'horodatage d'un message spécifique
    @Query("SELECT m.timestamp FROM Message m WHERE m.id = :messageId")
    Instant findTimestampById(@Param("messageId") Long messageId);
}
