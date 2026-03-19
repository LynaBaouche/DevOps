package com.etudlife.service;

import com.etudlife.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.etudlife.model.SavedJob;
import com.etudlife.model.JobStatus;

@Service
public class ChatService {

    private final PdfKnowledgeBase kb;
    private final GeminiClient gemini;
    private final ChatSessionService sessions;
    private final SavedJobService savedJobService;
    private final RecipeChatService recipeChatService;

    public ChatService(PdfKnowledgeBase kb, GeminiClient gemini, ChatSessionService sessions, SavedJobService savedJobService, RecipeChatService recipeChatService) {
        this.kb = kb;
        this.gemini = gemini;
        this.sessions = sessions;
        this.savedJobService = savedJobService;
        this.recipeChatService = recipeChatService;

    }
    public ChatResponse ask(String sessionId, String question, String mode,Long compteId)
    {
        //  stocker le message user dans Redis
        sessions.append(sessionId, "user", question);

        String q = (question == null) ? "" : question.trim();
        String qNorm = q.toLowerCase();

        // INTENT: offres marquées "INTERESSÉ"
        if (isJobsIntent(qNorm)) {
            JobStatus statusFiltre;
            String titre;

            if (qNorm.contains("postul")) {
                statusFiltre = JobStatus.POSTULE;
                titre = "📨 Vos offres pour lesquelles vous avez postulé";
            } else {
                statusFiltre = JobStatus.INTERESSE;
                titre = "🎯 Vos offres intéressantes";
            }

            List<SavedJob> jobs = savedJobService.getJobsByStatus(statusFiltre, compteId);

            String answer;
            if (jobs.isEmpty()) {
                answer = statusFiltre == JobStatus.POSTULE
                        ? "Vous n'avez postulé à aucune offre pour le moment."
                        : "Vous n'avez aucune offre marquée comme intéressante pour le moment.";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("JOB_TITLE:").append(titre).append("\n");
                for (SavedJob job : jobs) {
                    sb.append("JOB_ITEM:")
                            .append(job.getTitle() != null ? job.getTitle() : "Offre sans titre")
                            .append("|")
                            .append(job.getLocation() != null && !job.getLocation().isBlank() ? job.getLocation() : "Localisation non précisée")
                            .append("|")
                            .append(job.getApplyLink() != null ? job.getApplyLink() : "")
                            .append("\n");
                }
                answer = sb.toString().trim();
            }

            sessions.append(sessionId, "assistant", answer);
            return new ChatResponse(true, sessionId, question, answer, "saved-jobs");
        }




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

        if (recipeChatService.isRecipeQuestion(qNorm)) {
            var lastMessages = sessions.history(sessionId, 10);
            String chatHistory = lastMessages.isEmpty()
                    ? "(vide)"
                    : String.join("\n", lastMessages);

            String answer = recipeChatService.handleRecipeQuestion(question, chatHistory);
            sessions.append(sessionId, "assistant", answer);
            return new ChatResponse(true, sessionId, question, answer, "recipes-ai");
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


    private boolean isJobsIntent(String qNorm) {
        boolean hasVerbe = qNorm.contains("liste") || qNorm.contains("affiche")
                || qNorm.contains("donne") || qNorm.contains("montre")
                || qNorm.contains("voir") || qNorm.contains("quelles");

        boolean hasOffre = qNorm.contains("offre") || qNorm.contains("job")
                || qNorm.contains("stage") || qNorm.contains("candidature");

        boolean hasStatut = qNorm.contains("intéress") || qNorm.contains("interess")
                || qNorm.contains("interes")   || qNorm.contains("favori")
                || qNorm.contains("sauvegard") || qNorm.contains("postul")
                || qNorm.contains("marqué");

        boolean casSimple = qNorm.contains("mes offres") || qNorm.contains("mes jobs")
                || qNorm.contains("mes stages") || qNorm.contains("mes candidatures")
                || qNorm.contains("j'ai postulé") || qNorm.contains("j'ai postule");

        return casSimple || (hasVerbe && hasOffre) || (hasVerbe && hasStatut) || (hasOffre && hasStatut);
    }


}
