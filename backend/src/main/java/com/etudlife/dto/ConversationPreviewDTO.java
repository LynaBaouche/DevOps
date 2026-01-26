package com.etudlife.dto;

import java.time.LocalDateTime;

public class ConversationPreviewDTO {
    private Long conversationId;
    private String contactName;
    private Long contactId;
    private String lastMessageContent;
    private LocalDateTime lastMessageTimestamp;

    // Constructeur pour le mapping du Repository
    public ConversationPreviewDTO(Long conversationId, Long contactId, String contactName, String lastMessageContent, LocalDateTime lastMessageTimestamp) {
        this.conversationId = conversationId;
        this.contactId = contactId;
        this.contactName = contactName;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // + Getter/Setters
    public Long getConversationId() {
        return conversationId;
    }
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    public String getContactName() {
        return contactName;
    }
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
    public Long getContactId() {
        return contactId;
    }
    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }
    public String getLastMessageContent() {
        return lastMessageContent;
    }
    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }
    public LocalDateTime getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }
    public void setLastMessageTimestamp(LocalDateTime lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}
