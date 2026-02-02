package com.etudlife.controller;

import com.etudlife.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class GlobalMessageController {

    @Autowired
    private MessageService messageService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-ID", required = false) Long userId
    ) {
        try {
            // Assure-toi que cette méthode existe bien dans ton MessageService
            messageService.deleteMessage(id, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
