package com.etudlife.service;

import java.time.Instant;
import java.util.List;

import com.etudlife.model.Message;
import com.etudlife.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    // Méthode d'enregistrement
    public Message saveNewMessage(Long conversationId, Long senderId, String content) {
        // ** (Ajouter la validation de l'utilisateur ici) **

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        // Le timestamp est géré par l'entité/BDD

        return messageRepository.save(message);
    }

    // Méthode pour le Polling
    public List<Message> getNewMessagesAfter(Long conversationId, Long afterId) {
        // Récupérer le temps du message de référence
        Instant referenceTime = messageRepository.findTimestampById(afterId);

        // Trouver tous les messages plus récents que ce temps
        return messageRepository.findByConversationIdAndTimestampAfterOrderByTimestampAsc(
                conversationId,
                referenceTime
        );
    }

    // Méthode pour le chargement initial
    public List<Message> getLatestMessages(Long conversationId) {
        // Retourne les 50 derniers messages
        return messageRepository.findTop50ByConversationIdOrderByTimestampDesc(conversationId);
    }
}
