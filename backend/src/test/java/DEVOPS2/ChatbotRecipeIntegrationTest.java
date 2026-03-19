package DEVOPS2;

import com.etudlife.EtudlifeApp;
import com.etudlife.dto.ChatRequest;
import com.etudlife.service.ChatSessionService;
import com.etudlife.service.ChatStreamService;
import com.etudlife.service.GeminiClient;
import com.etudlife.service.PdfKnowledgeBase;
import com.etudlife.service.RecipeChatService;
import com.etudlife.service.SavedJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = EtudlifeApp.class
)
@AutoConfigureWebTestClient
class ChatbotRecipeIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatSessionService sessions;

    @MockBean
    private PdfKnowledgeBase kb;

    @MockBean
    private GeminiClient gemini;

    @MockBean
    private SavedJobService savedJobService;

    @MockBean
    private ChatStreamService chatStreamService;

    @MockBean
    private RecipeChatService recipeChatService;

    @BeforeEach
    void setup() {
        when(sessions.newSession()).thenReturn("sid-recipe");
        when(sessions.history(eq("sid-recipe"), anyInt()))
                .thenReturn(List.of("user:bonjour", "assistant:salut"));

        lenient().when(recipeChatService.isRecipeQuestion(anyString())).thenReturn(false);
    }

    @Test
    void message_recipeQuestion_shouldReturnRecipesAiSource() {
        when(recipeChatService.isRecipeQuestion("propose-moi une recette pas chère")).thenReturn(true);
        when(recipeChatService.handleRecipeQuestion(eq("Propose-moi une recette pas chère"), anyString()))
                .thenReturn("Nom du plat : Pâtes à la tomate. Ingrédients : pâtes, tomate, ail. Étapes : cuire les pâtes, préparer la sauce. Temps estimé : 15 min. Budget estimé : 4 €.");

        ChatRequest req = new ChatRequest();
        req.setQuestion("Propose-moi une recette pas chère");
        req.setMode("AUTO");

        webTestClient.post()
                .uri("/api/chat/message")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.sessionId").isEqualTo("sid-recipe")
                .jsonPath("$.source").isEqualTo("recipes-ai")
                .jsonPath("$.response").value(v ->
                        org.junit.jupiter.api.Assertions.assertTrue(
                                v.toString().contains("Nom du plat")
                                        || v.toString().toLowerCase().contains("pâtes")
                                        || v.toString().toLowerCase().contains("budget")
                        ));

        verify(recipeChatService).isRecipeQuestion("propose-moi une recette pas chère");
        verify(recipeChatService).handleRecipeQuestion(eq("Propose-moi une recette pas chère"), contains("user:bonjour"));
        verify(sessions).append("sid-recipe", "user", "Propose-moi une recette pas chère");
        verify(sessions).append(eq("sid-recipe"), eq("assistant"), contains("Nom du plat"));
        verifyNoInteractions(kb);
        verifyNoInteractions(gemini);
    }

    @Test
    void message_recipeQuestion_withIngredients_shouldReturnRecipesAiSource() {
        when(recipeChatService.isRecipeQuestion("j'ai des courgettes, tomates et poivrons, propose une recette")).thenReturn(true);
        when(recipeChatService.handleRecipeQuestion(eq("J'ai des courgettes, tomates et poivrons, propose une recette"), anyString()))
                .thenReturn("Nom du plat : Poêlée de légumes. Ingrédients : courgettes, tomates, poivrons, huile d'olive. Étapes : couper les légumes, faire revenir à la poêle. Temps estimé : 20 min. Budget estimé : 5 €.");

        ChatRequest req = new ChatRequest();
        req.setQuestion("J'ai des courgettes, tomates et poivrons, propose une recette");
        req.setMode("AUTO");

        webTestClient.post()
                .uri("/api/chat/message")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.source").isEqualTo("recipes-ai")
                .jsonPath("$.response").value(v ->
                        org.junit.jupiter.api.Assertions.assertTrue(
                                v.toString().toLowerCase().contains("courgettes")
                                        || v.toString().toLowerCase().contains("poêlée")
                                        || v.toString().toLowerCase().contains("poivrons")
                        ));

        verify(recipeChatService).handleRecipeQuestion(
                eq("J'ai des courgettes, tomates et poivrons, propose une recette"),
                contains("assistant:salut")
        );
        verifyNoInteractions(kb);
        verifyNoInteractions(gemini);
    }

    @Test
    void message_nonRecipeQuestion_shouldKeepOldBehavior() {
        ChatRequest req = new ChatRequest();
        req.setQuestion("bonjour");
        req.setMode("AUTO");

        webTestClient.post()
                .uri("/api/chat/message")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.source").isEqualTo("none")
                .jsonPath("$.response").value(v ->
                        org.junit.jupiter.api.Assertions.assertTrue(
                                v.toString().contains("Bienvenue sur EtudLife")
                        ));

        verifyNoInteractions(recipeChatService);
        verifyNoInteractions(kb);
        verifyNoInteractions(gemini);
    }
}