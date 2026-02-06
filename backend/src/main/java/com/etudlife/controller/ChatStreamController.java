package com.etudlife.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import com.etudlife.service.ChatStreamService;

@RestController
@RequestMapping("/api/chat")
public class ChatStreamController {

    private final ChatStreamService chatStreamService;

    public ChatStreamController(ChatStreamService chatStreamService) {
        this.chatStreamService = chatStreamService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam("question") String question,
            @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        return chatStreamService.streamAnswer(sessionId, question)
                .map(chunk -> ServerSentEvent.builder(chunk).event("chunk").build())
                .concatWithValues(ServerSentEvent.builder("[DONE]").event("done").build());
    }

}
