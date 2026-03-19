package com.etudlife.service;

import org.springframework.stereotype.Service;

@Service
public class RecipeChatService {

    private final GeminiClient gemini;

    public RecipeChatService(GeminiClient gemini) {
        this.gemini = gemini;
    }

    public boolean isRecipeQuestion(String question) {
        if (question == null) return false;

        String q = question.toLowerCase();

        return q.contains("recette")
                || q.contains("menu")
                || q.contains("repas")
                || q.contains("cuisine")
                || q.contains("manger")
                || q.contains("ingrédient")
                || q.contains("ingredient")
                || q.contains("budget")
                || q.contains("pas cher")
                || q.contains("pas chère")
                || q.contains("courgette")
                || q.contains("courgettes")
                || q.contains("tomate")
                || q.contains("tomates")
                || q.contains("poivron")
                || q.contains("poivrons")
                || q.contains("riz")
                || q.contains("pâtes")
                || q.contains("pates")
                || q.contains("thon")
                || q.contains("poulet")
                || q.contains("dîner")
                || q.contains("diner")
                || q.contains("déjeuner")
                || q.contains("dejeuner");
    }

    public String handleRecipeQuestion(String question, String chatHistory) {
        String prompt =
                "Tu es un assistant culinaire pour étudiants. " +
                        "Tu proposes des recettes simples, économiques et réalistes. " +
                        "Si l'utilisateur donne un budget, respecte ce budget autant que possible. " +
                        "Si l'utilisateur donne des ingrédients, base la recette principalement sur ces ingrédients. " +
                        "Si une recette ou une idée de plat a déjà été proposée dans l'historique, évite de reproposer exactement la même. " +
                        "Quand la question est répétée, propose une variante différente, avec un autre plat ou une autre combinaison. " +
                        "Réponds toujours en français. " +
                        "Structure obligatoire : Nom du plat, ingrédients, étapes, temps estimé, budget estimé. " +
                        "Ne parle pas d'EtudLife, ni de base de données, ni de documents. " +
                        "Historique de conversation : " + chatHistory + " " +
                        "Question utilisateur : " + question;

        return gemini.generate(prompt);
    }
}