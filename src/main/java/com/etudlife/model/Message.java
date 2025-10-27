package com.etudlife.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long conversationId;

    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String content; // Champ dont le getter manque

    private Instant timestamp;

    // --- CONSTRUCTEURS ---

    public Message() {
        this.timestamp = Instant.now();
    }

    // --- GETTERS & SETTERS (nécessaires pour JPA et la sérialisation JSON) ---

    // ... (autres getters et setters) ...

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    // ⭐ LE GETTER MANQUANT QUI CORRIGE L'ERREUR DE SÉRIALISATION ⭐
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
