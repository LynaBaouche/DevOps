package com.etudlife.service;

import java.util.Collections; // 👈 Nécessaire pour inverser la liste
import java.util.List;

import com.etudlife.model.Message;
import com.etudlife.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    // 🔑 Mise à jour de la signature de la méthode saveNewMessage
    public Message saveNewMessage(Long conversationId, Long senderId, Long receiverId, String content) {
        // ... (Logique de validation/sécurité de l'utilisateur ici) ...

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId); // 🔑 Enregistrement du destinataire
        message.setContent(content);

        return messageRepository.save(message);
    }

    // ----------------------------------------------------------------------
    // 1. 💡 MÉTHODE POUR LE POLLING (Correction : basée sur l'ID)
    // ----------------------------------------------------------------------
    public List<Message> getNewMessagesAfter(Long conversationId, Long afterId) {
        // Utilise la nouvelle méthode du Repository: récupère tous les messages
        // dont l'ID est supérieur à afterId, triés par timestamp ASC.
        return messageRepository.findByConversationIdAndIdGreaterThanOrderByTimestampAsc(
                conversationId,
                afterId
        );
    }

    // ----------------------------------------------------------------------
    // 2. 💡 MÉTHODE POUR LE CHARGEMENT INITIAL (Correction : inversion pour tri ASC)
    // ----------------------------------------------------------------------
    public List<Message> getLatestMessages(Long conversationId) {
        // 1. Récupère les 50 messages les plus récents (du plus récent au plus ancien)
        List<Message> latest = messageRepository.findTop50ByConversationIdOrderByTimestampDesc(conversationId);

        // 2. 🔑 INVERSION : On inverse la liste pour que le plus ancien des 50 soit en tête.
        // C'est l'ordre attendu par le front-end (du plus ancien au plus récent).
        Collections.reverse(latest);

        return latest;
    }
}