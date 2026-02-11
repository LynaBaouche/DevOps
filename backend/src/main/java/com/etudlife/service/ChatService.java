package com.etudlife.service;

import com.etudlife.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final PdfKnowledgeBase kb;
    private final GeminiClient gemini;
    private final ChatSessionService sessions;

    public ChatService(PdfKnowledgeBase kb, GeminiClient gemini, ChatSessionService sessions) {
        this.kb = kb;
        this.gemini = gemini;
        this.sessions = sessions;
    }

    public ChatResponse ask(String sessionId, String question) {

        //  stocker le message user dans Redis
        sessions.append(sessionId, "user", question);

        String q = (question == null) ? "" : question.trim();
        String qNorm = q.toLowerCase();


        if (qNorm.matches("^(ok\\s*)?(merci|mercii+|merci beaucoup|thx|thanks)(\\s*!*)?$")) {
            String answer = "Pas de souci 🙂 Si vous avez besoin d’autre chose, n’hésitez pas.";
            sessions.append(sessionId, "assistant", answer);
            return new ChatResponse(true, sessionId, question, answer, "none");
        }


        if (qNorm.matches("^(bonjour|salut|hello|hey|bonsoir)(\\s*!*)?$")) {
            String answer = "Bonjour, comment puis-je vous aider ?";
            sessions.append(sessionId, "assistant", answer);
            return new ChatResponse(true, sessionId, question, answer, "none");
        }

        //  récupérer un peu d’historique
        var lastMessages = sessions.history(sessionId, 10);
        String chatHistory = lastMessages.isEmpty()
                ? "(vide)"
                : String.join("\n", lastMessages);

        // RAG PDF
        List<PdfKnowledgeBase.Chunk> hits = kb.search(question);

        String context = hits.isEmpty()
                ? "Aucun extrait pertinent trouvé dans les PDFs."
                : hits.stream()
                .map(c -> "SOURCE: " + c.source() + "\nEXTRAIT: " + c.text())
                .collect(Collectors.joining("\n\n---\n\n"));

        //  prompt avec historique + contexte
        String prompt =
                "Tu es un assistant pour des étudiants. Réponds uniquement avec les infos du CONTEXTE.\n" +
                        "Si tu n'as pas l'information dans le CONTEXTE, ne mentionne pas de document, ne mentionne pas la charte.\n"
                        +
                        "Si le contexte ne contient pas la réponse, réponds simplement: \"Désolé, je n’ai pas d’information sur ce sujet.\".\n\n"
                        +
                        "HISTORIQUE:\n" + chatHistory + "\n\n" +
                        "CONTEXTE:\n" + context + "\n\n" +
                        "QUESTION: " + question + "\n\n" +
                        "RÉPONSE:";

        String answer = gemini.generate(prompt);

        // stocker la réponse bot
        sessions.append(sessionId, "assistant", answer);

        String sources = hits.stream()
                .map(PdfKnowledgeBase.Chunk::source)
                .distinct()
                .collect(Collectors.joining(", "));
        if (sources.isBlank()) sources = "none";

        return new ChatResponse(true, sessionId, question, answer, sources);
    }
}
