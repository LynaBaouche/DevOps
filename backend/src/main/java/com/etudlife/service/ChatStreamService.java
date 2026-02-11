package com.etudlife.service;

import com.etudlife.dto.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatStreamService {

    private final ChatService chatService;
    private final ChatSessionService sessions;

    public ChatStreamService(ChatService chatService, ChatSessionService sessions) {
        this.chatService = chatService;
        this.sessions = sessions;
    }

    public Flux<String> streamAnswer(String sessionId, String question) {

        // 1) crée une session si besoin
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = sessions.newSession();
        }

        // 2) appelle ton pipeline normal
        ChatResponse res = chatService.ask(sessionId, question);
        String full = (res.getResponse() == null) ? "" : res.getResponse();

        // (Optionnel) si tu veux que le front récupère la sessionId via SSE:
        // on envoie un “event” spécial au début (simple et pratique)
        String sid = sessionId;
        Flux<String> head = Flux.just("__SESSION__:" + sid);

        // 3) chunking
        List<String> chunks = chunkByWords(full, 2);

        Flux<String> body = Flux.fromIterable(chunks)
                .delayElements(Duration.ofMillis(35));

        return head.concatWith(body);
    }

    private List<String> chunkByWords(String text, int wordsPerChunk) {
        String[] words = text.split("\\s+");
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (String w : words) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(w);
            count++;

            if (count >= wordsPerChunk) {
                out.add(sb.toString());
                sb.setLength(0);
                count = 0;
            }
        }
        if (sb.length() > 0) out.add(sb.toString());
        return out;
    }
}
