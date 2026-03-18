package DEVOPS2;

import com.etudlife.EtudlifeApp;
import com.etudlife.dto.ChatRequest;
import com.etudlife.dto.ChatResponse;
import com.etudlife.service.ChatSessionService;
import com.etudlife.service.GeminiClient;
import com.etudlife.service.PdfKnowledgeBase;
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
class ChatbotConversationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatSessionService sessions;

    @MockBean
    private GeminiClient gemini;

    @MockBean
    private PdfKnowledgeBase kb;

    @MockBean
    private SavedJobService savedJobService;

    @BeforeEach
    void setup() {
        when(sessions.newSession()).thenReturn("sid-conv");

        when(gemini.generate(anyString()))
                .thenReturn("Réponse mock Gemini");

        when(sessions.history(eq("sid-conv"), anyInt()))
                .thenReturn(List.of(
                        "user:bonjour",
                        "assistant:Bienvenue",
                        "user:comment ça va ?",
                        "assistant:Réponse mock Gemini"
                ));
    }

    @Test
    void conversation_shouldKeepSameSession_andAppendMessages() {

        // 1️⃣ Création session
        webTestClient.post()
                .uri("/api/chat/new-session")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sessionId").isEqualTo("sid-conv");

        // 2️⃣ Premier message
        ChatRequest req1 = new ChatRequest();
        req1.setQuestion("bonjour");
        req1.setMode("AUTO");

        ChatResponse res1 = webTestClient.post()
                .uri("/api/chat/message")
                .bodyValue(req1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChatResponse.class)
                .returnResult()
                .getResponseBody();

        // 3️⃣ Deuxième message (même session)
        ChatRequest req2 = new ChatRequest();
        req2.setQuestion("comment ça va ?");
        req2.setMode("AUTO");
        req2.setSessionId(res1.getSessionId());

        ChatResponse res2 = webTestClient.post()
                .uri("/api/chat/message")
                .bodyValue(req2)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChatResponse.class)
                .returnResult()
                .getResponseBody();

        // 🔎 Vérifications
        assert res2 != null;
        assert res2.getSessionId().equals(res1.getSessionId());

        // Vérifie que les messages sont bien enregistrés
        verify(sessions).append(anyString(), eq("user"), eq("bonjour"));
        verify(sessions).append(anyString(), eq("user"), eq("comment ça va ?"));

        verify(sessions, atLeast(2))
                .append(anyString(), eq("assistant"), anyString());
    }

    @Test
    void history_shouldReturnFullConversation() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/chat/history")
                        .queryParam("sessionId", "sid-conv")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.messages[0]").isEqualTo("user:bonjour")
                .jsonPath("$.messages[1]").value(v ->
                        org.junit.jupiter.api.Assertions.assertTrue(v.toString().contains("assistant:"))
                );
    }
}