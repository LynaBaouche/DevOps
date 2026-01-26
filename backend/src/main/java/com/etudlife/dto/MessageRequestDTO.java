package com.etudlife.dto;

public class MessageRequestDTO {
    // Note: Ignoré par l'API pour la sécurité, mais inclus pour la cohérence
    private Long senderId;

    // 🔑 NOUVEAU: ID de la personne à qui le message est destiné
    private Long receiverId;

    private String content;

    // --- Getters & Setters ---
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; } // 👈 NOUVEAU GETTER

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}