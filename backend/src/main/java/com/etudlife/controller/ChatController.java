package com.etudlife.controller;

import com.etudlife.dto.ChatRequest;
import com.etudlife.dto.ChatResponse;
import com.etudlife.service.ChatService;
import com.etudlife.service.ChatSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatSessionService sessions;

    public ChatController(ChatService chatService, ChatSessionService sessions) {
        this.chatService = chatService;
        this.sessions = sessions;
    }

    // 1) créer une session
    @PostMapping("/new-session")
    public ResponseEntity<?> newSession() {
        String sessionId = sessions.newSession();
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    // 2) récupérer historique
    @GetMapping("/history")
    public ResponseEntity<?> history(@RequestParam String sessionId) {
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "messages", sessions.history(sessionId, 50)
        ));
    }

    // 3) fermer le chat => supprimer en Redis
    @PostMapping("/close")
    public ResponseEntity<?> close(@RequestParam String sessionId) {
        sessions.delete(sessionId);
        return ResponseEntity.ok().build();
    }

    // 4) envoyer un message
    @PostMapping("/message")
    public ResponseEntity<?> message(@RequestBody ChatRequest req) {
        if (req == null || req.getQuestion() == null || req.getQuestion().isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'question'");
        }

        String sessionId = req.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = sessions.newSession();
        }

        ChatResponse res = chatService.ask(sessionId, req.getQuestion());
        return ResponseEntity.ok(res);
    }
}
