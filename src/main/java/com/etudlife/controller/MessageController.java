package com.etudlife.controller;

import com.etudlife.dto.MessageRequestDTO;
import com.etudlife.model.Message;
import com.etudlife.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // ** 1. Endpoint POST : Envoyer un message **
    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody MessageRequestDTO request,
            @RequestHeader(value = "X-User-ID", required = true) Long authenticatedUserId
    ) {
        Long receiverId = request.getReceiverId();

        if (receiverId == null) {
            // 400 si le destinataire est manquant (problème client/interface)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Message newMessage = messageService.saveNewMessage(
                conversationId,
                authenticatedUserId,
                receiverId,
                request.getContent()
        );

        // 🔑 Le corps de la réponse contient l'objet Message sérialisé
        return new ResponseEntity<>(newMessage, HttpStatus.CREATED);
    }

    // ** 2. Endpoint GET : Récupérer les messages (Historique & Polling) **
    @GetMapping
    public List<Message> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long afterId) {

        if (afterId != null) {
            return messageService.getNewMessagesAfter(conversationId, afterId);
        } else {
            return messageService.getLatestMessages(conversationId);
        }
    }
}