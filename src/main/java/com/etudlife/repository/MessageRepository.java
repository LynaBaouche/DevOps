package com.etudlife.repository;

import java.time.Instant;
import java.util.List;

import com.etudlife.dto.ConversationPreviewDTO;
import com.etudlife.dto.ConversationPreviewProjection;
import com.etudlife.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 1. Récupérer les 50 derniers messages d'une conversation (Historique initial)
    // Dans MessageRepository.java
    @Query(value = "SELECT m FROM Message m WHERE m.conversationId = :id ORDER BY m.timestamp DESC LIMIT 50")
    List<Message> findTop50ByConversationIdOrderByTimestampDesc(@Param("id") Long id);

    // 2. Récupérer les messages après un certain ID (Polling)
    // Tri par 'timestamp' croissant (ASC) est correct pour le Polling.
    List<Message> findByConversationIdAndIdGreaterThanOrderByTimestampAsc(
            Long conversationId, Long afterId
    );

    /**
     * Récupère l'aperçu de toutes les conversations de l'utilisateur.
     * Utilisation de SQL Natif pour garantir que la requête de MAX/GROUP BY complexe fonctionne correctement.
     * @param userId L'ID de l'utilisateur connecté.
     */
    @Query(value =
            "SELECT " +
                    "   m.conversation_id AS conversationId, " +
                    "   CONCAT(c_contact.prenom, ' ', c_contact.nom) AS contactName, " +
                    "   m.content AS lastMessageContent, " +
                    "   m.timestamp AS lastMessageTimestamp " +
                    "FROM messages m " +

                    // 1. Jointure avec le CONTACT (c_contact) : Utilisation de CASE WHEN pour trouver l'autre ID
                    //    LEFT JOIN est utilisé pour la tolérance en cas d'ID NULL.
                    "LEFT JOIN compte c_contact ON c_contact.id = (" +
                    "    CASE " +
                    "        WHEN m.sender_id = :userId THEN m.receiver_id " + // Si j'ai envoyé, le contact est le destinataire
                    "        ELSE m.sender_id " + // Si l'autre a envoyé, le contact est l'expéditeur
                    "    END" +
                    ") " +

                    // 2. Filtre: S'assurer que le message est le dernier de sa conversation (MAX TIMESTAMP)
                    "WHERE m.timestamp = (" +
                    "   SELECT MAX(m2.timestamp) FROM messages m2 " +
                    "   WHERE m2.conversation_id = m.conversation_id" +
                    ") " +

                    // 3. Filtre: S'assurer que l'utilisateur participe à la conversation (et la conversation est binaire)
                    "AND m.conversation_id IN (" +
                    "   SELECT DISTINCT m1.conversation_id FROM messages m1 WHERE m1.sender_id = :userId OR m1.receiver_id = :userId" +
                    ") " +

                    "ORDER BY m.timestamp DESC",
            nativeQuery = true
    )
    List<ConversationPreviewProjection> findConversationPreviewsByUserId(@Param("userId") Long userId);
}
