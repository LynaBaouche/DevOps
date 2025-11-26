package com.etudlife.service;

import java.util.Collections;
import java.util.List;

import com.etudlife.model.Message;
import com.etudlife.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    // 🔑 Signature mise à jour
    public Message saveNewMessage(Long conversationId, Long senderId, Long receiverId, String content) {

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId); // 🔑 Enregistrement du destinataire
        message.setContent(content);

        return messageRepository.save(message);
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
}