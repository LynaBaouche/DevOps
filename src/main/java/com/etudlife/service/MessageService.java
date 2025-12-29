package com.etudlife.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.etudlife.model.Message;
import com.etudlife.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.etudlife.model.NotificationType;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final NotificationService notificationService;

    @Autowired
    public MessageService(MessageRepository messageRepository,
                          NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
    }

    public Message saveNewMessage(Long conversationId, Long senderId, Long receiverId, String content) {

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);

        Message saved = messageRepository.save(message);

        // 🔔 notification au destinataire
        notificationService.create(
                receiverId,
                NotificationType.NEW_MESSAGE,
                "Nouveau message reçu",
                "/messages.html?conversationId=" + conversationId
        );

        return saved;
    }






    // ... (Autres méthodes de service)
    // Méthode pour le Polling
    public List<Message> getNewMessagesAfter(Long conversationId, Long afterId) {
        return messageRepository.findByConversationIdAndIdGreaterThanOrderByTimestampAsc(
                conversationId,
                afterId
        );
    }

    // Méthode pour le chargement initial
    public List<Message> getLatestMessages(Long conversationId) {
        List<Message> latest = messageRepository.findTop50ByConversationIdOrderByTimestampDesc(conversationId);
        Collections.reverse(latest);
        return latest;
    }

    public void deleteMessage(Long messageId, Long userId) {
        // 1. Chercher le message
        Optional<Message> messageOpt = messageRepository.findById(messageId);

        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();

            // 2. VÉRIFICATION DE SÉCURITÉ IMPORTANTE
            // On vérifie que l'utilisateur qui demande la suppression est bien l'auteur
            if (message.getSenderId().equals(userId)) {
                messageRepository.delete(message);
            } else {
                throw new RuntimeException("Vous n'avez pas le droit de supprimer ce message.");
            }
        } else {
            throw new RuntimeException("Message introuvable.");
        }
    }
}