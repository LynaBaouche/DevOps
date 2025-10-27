package com.etudlife.dto;

public class MessageRequestDTO {

    // L'ID de l'utilisateur qui ENVOIE le message.
    // Ce champ doit correspondre à l'utilisateur authentifié (vérification dans le Controller).
    private Long senderId;

    // Le contenu textuel du message.
    private String content;

    // Vous pouvez ajouter d'autres champs si votre API en a besoin,
    // par exemple: private String clientId; pour suivre l'appareil.

    // --- GETTERS & SETTERS (Nécessaires pour la désérialisation JSON par Spring) ---

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
