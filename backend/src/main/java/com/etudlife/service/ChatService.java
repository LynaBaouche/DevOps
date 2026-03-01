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
    private final SavedJobService savedJobService;

    public ChatService(PdfKnowledgeBase kb, GeminiClient gemini, ChatSessionService sessions, SavedJobService savedJobService, SavedJobService savedJobService1) {
        this.kb = kb;
        this.gemini = gemini;
        this.sessions = sessions;
        this.savedJobService = savedJobService1;

    }

    public ChatResponse ask(String sessionId, String question, String mode)
    {

        //  stocker le message user dans Redis
        sessions.append(sessionId, "user", question);

        String q = (question == null) ? "" : question.trim();
        String qNorm = q.toLowerCase();


        // FORCE "sourcee.pdf" si on parle d'annonce, de compte, de menu ou de bouton
        String fileName = (qNorm.contains("annonce") || qNorm.contains("étape") || qNorm.contains("créer") || qNorm.contains("compte"))
                ? "sourcee.pdf"
                : "charte.pdf";

        if (qNorm.matches("^(ok\\s*)?(merci|mercii+|merci beaucoup|thx|thanks)(\\s*!*)?$")) {
            String answer = "Pas de souci 🙂 Si vous avez besoin d’autre chose, n’hésitez pas.";
            sessions.append(sessionId, "assistant", answer);
            return new ChatResponse(true, sessionId, question, answer, "none");
        }


        if (qNorm.matches("^(bonjour|salut|hello|hey|bonsoir)(\\s*!*)?$")) {
            String answer = "Bienvenue sur EtudLife !\n\n" +
            "EtudLife est votre plateforme étudiante pour :\n" +
                    " Gérer votre agenda et celui de vos proches\n" +
                    " Échanger via la messagerie\n" +
                    " Publier ou consulter des annonces\n" +
                    " Postuler à des offres de stage ou d’alternance\n" +
                    " Réserver des livres et des salles à la bibliothèque\n" +
                    "️ Organiser votre quotidien avec des recettes étudiantes\n\n" +
                    "Je peux également répondre à vos questions concernant le règlement intérieur et la charte de l’Université Paris Nanterre.\n\n" +
                    "Comment puis-je vous aider aujourd’hui ?";
            sessions.append(sessionId, "assistant", answer);
            return new ChatResponse(true, sessionId, question, answer, "none");
        }

        //  récupérer un peu d’historique
        var lastMessages = sessions.history(sessionId, 10);
        String chatHistory = lastMessages.isEmpty()
                ? "(vide)"
                : String.join("\n", lastMessages);

        // RAG PDF
        String m = normalizeMode(mode);

// AUTO -> on décide selon la question
        if (m.equals("AUTO")) {
            m = looksLikeSiteQuestion(qNorm) ? "SITE" : "REGLEMENT";
        }

// Recherche filtrée
        // Appelle bien searchInFile avec ce fileName
        List<PdfKnowledgeBase.Chunk> hits = kb.searchInFile(question, fileName);

// Fallback : si rien trouvé dans ce mode, tente l'autre mode
        if (hits.isEmpty()) {
            String other = m.equals("SITE") ? "REGLEMENT" : "SITE";
            List<PdfKnowledgeBase.Chunk> hits2 = kb.search(question, other);
            if (!hits2.isEmpty()) {
                hits = hits2;
                m = other;
            }
        }


        // ✅ Si rien trouvé : on force une réponse “safe” et on n'appelle pas Gemini
        if (hits.isEmpty()) {
            String answer = "Désolé, je n’ai pas d’information sur ce sujet.";
            sessions.append(sessionId, "assistant", answer);
            return new ChatResponse(true, sessionId, question, answer, "none");
        }

        String context = hits.stream()
                .map(c -> "SOURCE: " + c.source() + "\nEXTRAIT: " + c.text())
                .collect(Collectors.joining("\n\n---\n\n"));


        //  prompt avec historique + contexte
        String prompt =
                "Tu es un assistant pour des étudiants.\n" +
                        "Réponds uniquement avec les informations présentes dans le CONTEXTE.\n" +
                        "Si le CONTEXTE ne contient pas la réponse, réponds exactement : \"Désolé, je n’ai pas d’information sur ce sujet.\".\n" +
                        "Ne mentionne jamais la charte ni le document source.\n" +
                        "Rédige toujours la réponse sous forme d’un paragraphe fluide.\n" +
                        "N’utilise pas de listes, pas de puces, pas d’astérisques, pas de tirets.\n" +
                        "Ne fais pas de retour à la ligne entre chaque phrase.\n\n" +
                        "Tu peux reformuler la question pour chercher l'information la plus proche dans le CONTEXTE.\n"+

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
    private String normalizeMode(String mode) {
        if (mode == null) return "AUTO";
        String m = mode.trim().toUpperCase();
        if (m.equals("SITE") || m.equals("REGLEMENT") || m.equals("AUTO")) return m;
        return "AUTO";
    }

    private boolean looksLikeSiteQuestion(String qNorm) {
        return qNorm.contains("annonce")
                || qNorm.contains("favori")
                || qNorm.contains("profil")
                || qNorm.contains("compte")
                || qNorm.contains("connexion")
                || qNorm.contains("connecter")
                || qNorm.contains("déconnexion")
                || qNorm.contains("deconnecter")
                || qNorm.contains("mot de passe")
                || qNorm.contains("inscription")
                || qNorm.contains("publier")
                || qNorm.contains("supprimer")
                || qNorm.contains("modifier");
    }


    private boolean isInterestedJobsIntent(String qNorm) {
        return (qNorm.contains("liste") || qNorm.contains("affiche") || qNorm.contains("donne"))
                && (qNorm.contains("offre") || qNorm.contains("annonce") || qNorm.contains("job") || qNorm.contains("candidature"))
                && (qNorm.contains("intéress") || qNorm.contains("interess") || qNorm.contains("favori") || qNorm.contains("sauvegard"));
    }


}
