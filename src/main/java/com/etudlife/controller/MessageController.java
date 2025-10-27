package com.etudlife.controller;

import java.util.List;

import com.etudlife.dto.MessageRequestDTO;
import com.etudlife.model.Message;
import com.etudlife.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // ** 1. Endpoint POST : Envoyer un message **
    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody MessageRequestDTO request) {

        // ** TODO: Remplacer 123L par l'ID de l'utilisateur authentifié réel **
        Long authenticatedUserId = 123L;

        if (!authenticatedUserId.equals(request.getSenderId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }

        Message newMessage = messageService.saveNewMessage(
                conversationId,
                request.getSenderId(),
                request.getContent()
        );

        return new ResponseEntity<>(newMessage, HttpStatus.CREATED); // 201
    }

    // ** 2. Endpoint GET : Récupérer les messages (Historique & Polling) **
    @GetMapping
    public List<Message> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long afterId) { // afterId est utilisé pour le Polling

        if (afterId != null) {
            // Polling : on demande les nouveaux messages depuis le dernier message connu (afterId)
            return messageService.getNewMessagesAfter(conversationId, afterId);
        } else {
            // Chargement initial de l'historique
            return messageService.getLatestMessages(conversationId);
        }
    }
}
