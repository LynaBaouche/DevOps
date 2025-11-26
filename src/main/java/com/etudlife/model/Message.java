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

    // 🔑 NOUVEAU CHAMP : Destinataire du message
    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant timestamp;

    public Message() {
        this.timestamp = Instant.now();
    }

    // --- GETTERS & SETTERS (Seuls les nouveaux et modifiés sont montrés) ---

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    // 🔑 GETTER/SETTER pour le ReceiverId
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
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
