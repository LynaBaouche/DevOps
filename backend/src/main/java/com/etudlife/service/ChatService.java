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
        sessions.append(sessionId, "user", question);

        String q = (question == null) ? "" : question.trim();
        String qNorm = q.toLowerCase();

        // --- SALUTATIONS ET REMERCIEMENTS (GARDÉS) ---
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

        // --- LOGIQUE RAG ---
        // 1. Choix du fichier
        String fileName = (qNorm.contains("annonce") || qNorm.contains("compte") || qNorm.contains("favori"))
                ? "sourcee.pdf"
                : "charte.pdf";


        List<PdfKnowledgeBase.Chunk> hits = kb.searchInFile(question, fileName);

        // 3. Construction du contexte textuel pour l'IA
        String context = hits.stream()
                .map(c -> "SOURCE: " + c.source() + "\nEXTRAIT: " + c.text())
                .collect(Collectors.joining("\n\n---\n\n"));

        if (context.isBlank()) {
            context = "Aucune information spécifique trouvée dans les guides.";
        }

        // 4. Historique
        var lastMessages = sessions.history(sessionId, 10);
        String chatHistory = lastMessages.isEmpty() ? "(vide)" : String.join("\n", lastMessages);

        // 5. Prompt final avec CONTEXTE inclus
        String prompt = "Tu es l'assistant officiel d'EtudLife. Réponds uniquement avec les informations du CONTEXTE fourni.\n" +
                "Si la réponse n'est pas dans le CONTEXTE, réponds : \"Désolé, je n’ai pas d’information sur ce sujet dans mes guides.\".\n" +
                "Ne mentionne jamais le nom des fichiers sources dans ta réponse.\n\n" +
                "HISTORIQUE DES ÉCHANGES :\n" + chatHistory + "\n\n" +
                "CONTEXTE :\n" + context + "\n\n" + // <-- CRUCIAL : On envoie les infos ici
                "QUESTION : " + question + "\n\n" +
                "RÉPONSE :";

        // 6. Appel à l'IA et stockage
        String answer = gemini.generate(prompt);
        sessions.append(sessionId, "assistant", answer);

        // 7. Calcul des sources pour le retour JSON
        String sources = hits.stream()
                .map(PdfKnowledgeBase.Chunk::source)
                .distinct()
                .collect(Collectors.joining(", "));

        if (sources.isEmpty()) sources = "none";

        return new ChatResponse(true, sessionId, question, answer, sources);
    }
}