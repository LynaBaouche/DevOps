package com.etudlife.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final WebClient webClient;

    @Value("${ai.gemini.apiKey}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String model;

    public GeminiClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Clé GEMINI_API_KEY manquante côté backend.";
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            Map resp = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")

                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            try {
                List candidates = (List) resp.get("candidates");
                Map c0 = (Map) candidates.get(0);
                Map content = (Map) c0.get("content");
                List parts = (List) content.get("parts");
                Map p0 = (Map) parts.get(0);
                return String.valueOf(p0.get("text"));
            } catch (Exception parse) {
                return "Réponse Gemini invalide: " + resp;
            }

        } catch (Exception e) {
            return "Erreur Gemini: " + e.getMessage();
        }
    }
}
