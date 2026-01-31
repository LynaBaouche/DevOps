package com.etudlife.controller;

import com.etudlife.dto.ChatAskRequest;
import com.etudlife.dto.ChatAskResponse;
import com.etudlife.service.AgentIAService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AgentIAService agent;

    public ChatController(AgentIAService agent) {
        this.agent = agent;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatAskResponse> sendMessage(@RequestBody ChatAskRequest request) {
        ChatAskResponse resp = agent.ask(request.getQuestion());
        return ResponseEntity.ok(resp);
    }
}
