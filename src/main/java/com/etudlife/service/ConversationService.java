package com.etudlife.service;

import com.etudlife.dto.ConversationPreviewDTO;
import com.etudlife.dto.ConversationPreviewProjection;
import com.etudlife.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private MessageRepository messageRepository;

    public List<ConversationPreviewDTO> getPreviewsByUserId(Long userId) {

        List<ConversationPreviewProjection> projections = messageRepository.findConversationPreviewsByUserId(userId);

        // Retourner les projections, car Spring est souvent capable de gérer cela
        // si l'interface et la classe DTO correspondent.
        // Dans ce cas, nous devons changer le Controller pour qu'il attende List<ConversationPreviewProjection>
        // Si cela échoue, il faudra un mapping manuel ou une interface DTO à la place de la classe.

        // Option la plus simple et la plus rapide :
        // Si la liste ne contient que des projections, on renvoie la projection.
        // Vous devez changer la signature du contrôleur pour qu'il retourne List<ConversationPreviewProjection>
        return (List<ConversationPreviewDTO>)(List<?>) projections;
    }
}
