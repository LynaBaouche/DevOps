package com.etudlife.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GeminiClient {


    private final WebClient webClient;

    @Value("${ai.gemini.apiKey}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.0-flash}")
    private String model;

    @Autowired
    private PdfKnowledgeBase pdfKnowledgeBase;

    public GeminiClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    // Méthode principale pour envoyer un message à l'IA
    public String ask(String userMessage) {
        // 1. On cherche dans les PDF en utilisant userMessage (qui remplace 'question')
        List<PdfKnowledgeBase.Chunk> contextChunks = pdfKnowledgeBase.search(userMessage);

        // 2. Transformer ces extraits en texte
        String context = contextChunks.stream()
                .map(PdfKnowledgeBase.Chunk::text)
                .collect(Collectors.joining("\n"));

        // 3. Préparer le prompt pour Gemini
        String systemPrompt = """
            Tu es l’assistant support du site EtudLife.
            Tu réponds UNIQUEMENT à partir du guide fourni dans CONTEXTE.
            Tu dois répondre avec des étapes claires (1., 2., 3.) et des noms de menus/boutons.
            Si l’information n’est pas dans le CONTEXTE, tu réponds :
            "Je n’ai pas cette information dans le guide. Dis-moi sur quelle page tu es et je te guiderai."
            """;

        String finalPrompt = systemPrompt
                + "\n\nCONTEXTE :\n" + context
                + "\n\nQUESTION : " + userMessage;

        // 4. Appeler la méthode generate
        return this.generate(finalPrompt);
    }

    // Méthode technique d'appel API
    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Clé GEMINI_API_KEY manquante.";
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

            List candidates = (List) resp.get("candidates");
            Map c0 = (Map) candidates.get(0);
            Map content = (Map) c0.get("content");
            List parts = (List) content.get("parts");
            Map p0 = (Map) parts.get(0);
            return String.valueOf(p0.get("text"));

        } catch (Exception e) {
            return "Erreur Gemini: " + e.getMessage();
        }
    }
}