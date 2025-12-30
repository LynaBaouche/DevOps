package com.etudlife.dto;
// Vous pouvez la mettre dans le même package que votre DTO ou dans un package 'projection'

import java.time.LocalDateTime;

public interface ConversationPreviewProjection {

    // Doit correspondre à l'alias SQL 'conversationId'
    Long getConversationId();

    // Doit correspondre à l'alias SQL 'contactName'
    String getContactName();

    // Doit correspondre à l'alias SQL 'lastMessageContent' (votre champ 'content')
    String getLastMessageContent();

    LocalDateTime getLastMessageTimestamp();

    Long getContactId();

    // Note: Pas de setters, c'est une projection en lecture seule.
}