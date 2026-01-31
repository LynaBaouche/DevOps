package com.etudlife.service;

import com.etudlife.dto.ChatAskResponse;
import org.springframework.stereotype.Service;

@Service
public class AgentIAService {

    private final HuggingFaceClient hf;
    private final DocumentIAService docs;
    private final ScopeGuardService scope;
    private final AnonymizerService anonymizer;

    public AgentIAService(HuggingFaceClient hf,
                          DocumentIAService docs,
                          ScopeGuardService scope,
                          AnonymizerService anonymizer) {
        this.hf = hf;
        this.docs = docs;
        this.scope = scope;
        this.anonymizer = anonymizer;
    }

    public ChatAskResponse ask(String question) {
        if (question == null || question.trim().isBlank()) {
            return new ChatAskResponse(false, question, "Question vide", "low", "autre", "none");
        }

        // ✅ Confidentialité : anonymisation avant appel API externe
        String clean = anonymizer.anonymize(question);

        // ✅ Scope rule : pas d'appel externe si hors-sujet
        if (!scope.isInScope(clean)) {
            return new ChatAskResponse(true, question,
                    "Je suis un assistant administratif EtudLife. Je réponds uniquement aux questions liées à la vie universitaire (absences, examens, handicap, inscription...).",
                    "high", "hors-scope", "none");
        }

        // ✅ Choix du PDF selon mots-clés (TES noms exacts)
        String category;
        String sourceFile;

        String q = clean.toLowerCase();

        if (q.contains("handicap") || q.contains("aménagement") || q.contains("amenagement") || q.contains("tiers temps")) {
            category = "handicap";
            sourceFile = "CHARTE HANDICAP adoptée en CFVU_5 déc. 2023.pdf";
        } else if (q.contains("examen") || q.contains("examens") || q.contains("rattrapage")) {
            category = "examens";
            sourceFile = "CHARTE EXAMENS UPN vote CFVU Juin 2020.pdf";
        } else if (q.contains("plagiat") || q.contains("triche") || q.contains("copie")) {
            category = "plagiat";
            sourceFile = "Charte anti-plagiat approuvée au CAC du 11.12.2018.pdf";
        } else if (q.contains("association") || q.contains("asso")) {
            category = "associations";
            sourceFile = "Charte des associations UPN.pdf";
        } else if (q.contains("échange") || q.contains("echange") || q.contains("international")) {
            category = "international";
            sourceFile = "charte des échanges internationaux signée.pdf";
        } else {
            category = "autre";
            // fallback : examens (tu peux changer si tu ajoutes un règlement intérieur plus tard)
            sourceFile = "CHARTE EXAMENS UPN vote CFVU Juin 2020.pdf";
        }

        // ✅ Extraction PDF → texte (RAG)
        String context = docs.getPdfText(sourceFile);

        // Sécurité : si extraction vide
        if (context == null || context.isBlank()) {
            return new ChatAskResponse(true, question,
                    "Je ne parviens pas à lire le document officiel pour le moment. Veuillez contacter le secrétariat à secretariat@fac.fr.",
                    "low", category, sourceFile);
        }

        // Limiter taille du contexte (sinon prompt trop long)
        if (context.length() > 9000) {
            context = context.substring(0, 9000);
        }

        // ✅ Prompt système (Autorité + Fallback imposé)
        String systemPrompt =
                "Tu es un personnel administratif universitaire.\n" +
                        "Tu dois répondre UNIQUEMENT à partir du CONTEXTE OFFICIEL fourni.\n" +
                        "Interdiction d'utiliser des connaissances générales d'internet.\n" +
                        "Tu dois citer une référence interne (ex: 'Article', 'Chapitre', 'Section', ou un intitulé exact).\n" +
                        "Si l'information n'est pas trouvée dans le contexte, répond EXACTEMENT :\n" +
                        "\"Je ne trouve pas cette information dans le règlement. Veuillez contacter le secrétariat à secretariat@fac.fr.\"\n" +
                        "Si la question est hors vie universitaire, refuse poliment.\n\n" +
                        "=== CONTEXTE OFFICIEL (" + sourceFile + ") ===\n" +
                        context + "\n" +
                        "=== FIN CONTEXTE ===\n\n" +
                        "Question : " + clean + "\n" +
                        "Réponds en français, de manière claire et courte.";

        // ✅ Appel externe (avec fallback si API down)
        String raw = hf.generate(systemPrompt);

        if (raw == null || raw.startsWith("HF_ERROR")) {
            return new ChatAskResponse(true, question,
                    "Service momentanément indisponible. Réessaie dans quelques instants.",
                    "low", category, sourceFile);
        }

        String answer = raw;

        // ✅ Post-check anti-hallucination : il faut une “référence”
        String low = answer.toLowerCase();
        boolean hasRef = low.contains("article") || low.contains("chapitre") || low.contains("section");

        if (!hasRef) {
            answer = "Je ne trouve pas cette information dans le règlement. Veuillez contacter le secrétariat à secretariat@fac.fr.";
            return new ChatAskResponse(true, question, answer, "low", category, sourceFile);
        }

        return new ChatAskResponse(true, question, answer, "medium", category, sourceFile);
    }
}
