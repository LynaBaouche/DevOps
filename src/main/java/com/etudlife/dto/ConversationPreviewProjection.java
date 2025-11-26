package com.etudlife.dto;
// Vous pouvez la mettre dans le même package que votre DTO ou dans un package 'projection'

import java.time.Instant;

public interface ConversationPreviewProjection {

    // Doit correspondre à l'alias SQL 'conversationId'
    Long getConversationId();

    // Doit correspondre à l'alias SQL 'contactName'
    String getContactName();

    // Doit correspondre à l'alias SQL 'lastMessageContent' (votre champ 'content')
    String getLastMessageContent();

    // Doit correspondre à l'alias SQL 'lastMessageTimestamp' (votre champ 'timestamp')
    Instant getLastMessageTimestamp();

    Long getContactId();

    // Note: Pas de setters, c'est une projection en lecture seule.
}