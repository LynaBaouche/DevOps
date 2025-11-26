package com.etudlife.controller;

import com.etudlife.dto.ConversationPreviewDTO;
import com.etudlife.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader; // 👈 Importation clé
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<ConversationPreviewDTO>> getConversationPreviews(
            @RequestHeader(value = "X-User-ID", required = true) Long authenticatedUserId // 👈 ID lu du header
    ) {
        // L'ID est garanti d'être présent (required=true) et converti en Long.

        List<ConversationPreviewDTO> previews = conversationService.getPreviewsByUserId(authenticatedUserId);

        return ResponseEntity.ok(previews);
    }
}