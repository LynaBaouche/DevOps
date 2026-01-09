package com.etudlife.service;

import com.etudlife.dto.ConversationPreviewDTO;
import com.etudlife.dto.ConversationPreviewProjection;
import com.etudlife.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    @Autowired
    private MessageRepository messageRepository;

    public List<ConversationPreviewDTO> getPreviewsByUserId(Long userId) {

        List<ConversationPreviewProjection> projections = messageRepository.findConversationPreviewsByUserId(userId);

        return projections.stream()
                .map(p -> new ConversationPreviewDTO(
                        p.getConversationId(),
                        p.getContactId(), // 🔑 Maintenant disponible
                        p.getContactName(),
                        p.getLastMessageContent(),
                        p.getLastMessageTimestamp()
                ))
                .collect(Collectors.toList());
    }

    public Long getOrInitConversationId(Long user1, Long user2) {
        // 1. Chercher si une conversation existe déjà
        Long existingId = messageRepository.findConversationIdByParticipants(user1, user2);

        if (existingId != null) {
            return existingId;
        } else {
            // 2. Sinon, on génère un nouvel ID unique
            // (Utiliser System.currentTimeMillis est une façon simple de générer un ID unique)
            return System.currentTimeMillis();
        }
    }
}
