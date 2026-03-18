package DEVOPS2;

import com.etudlife.EtudlifeApp;
import com.etudlife.dto.ChatRequest;
import com.etudlife.dto.ChatResponse;
import com.etudlife.service.ChatSessionService;
import com.etudlife.service.ChatStreamService;
import com.etudlife.service.GeminiClient;
import com.etudlife.service.PdfKnowledgeBase;
import com.etudlife.service.SavedJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = EtudlifeApp.class
)
@AutoConfigureWebTestClient
class ChatbotIntegrationTest {

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

    @BeforeEach
    void setup() {
        when(sessions.newSession()).thenReturn("sid-test");

        when(sessions.history(eq("sid-test"), anyInt()))
                .thenReturn(List.of(
                        "user:bonjour",
                        "assistant:Bienvenue sur EtudLife !"
                ));

        when(chatStreamService.streamAnswer(
                isNull(),
                eq("bonjour"),
                eq("AUTO"),
                isNull()
        )).thenReturn(Flux.just(
                "__SESSION__:sid-test",
                "chunk-1",
                "chunk-2"
        ));
    }

    @Test
    void newSession_shouldReturnSessionId() {
        webTestClient.post()
                .uri("/api/chat/new-session")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sessionId").isEqualTo("sid-test");

        verify(sessions).newSession();
    }

    @Test
    void message_missingQuestion_shouldReturn400() {
        webTestClient.post()
                .uri("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Missing 'question'");
    }

    @Test
    void message_bonjour_shouldReturnPresentation() {
        ChatRequest req = new ChatRequest();
        req.setQuestion("bonjour");
        req.setMode("AUTO");

        ChatResponse response = webTestClient.post()
                .uri("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChatResponse.class)
                .returnResult()
                .getResponseBody();

        assert response != null;
        org.junit.jupiter.api.Assertions.assertTrue(response.isSuccess());
        org.junit.jupiter.api.Assertions.assertEquals("sid-test", response.getSessionId());
        org.junit.jupiter.api.Assertions.assertEquals("none", response.getSource());
        org.junit.jupiter.api.Assertions.assertTrue(
                response.getResponse().contains("Bienvenue sur EtudLife")
        );

        verify(sessions).append("sid-test", "user", "bonjour");
        verify(sessions).append(eq("sid-test"), eq("assistant"), argThat(containsWelcomeText()));
    }

    @Test
    void history_shouldReturnMessages() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/chat/history")
                        .queryParam("sessionId", "sid-test")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sessionId").isEqualTo("sid-test")
                .jsonPath("$.messages[0]").isEqualTo("user:bonjour")
                .jsonPath("$.messages[1]").value(v ->
                        org.junit.jupiter.api.Assertions.assertTrue(v.toString().contains("assistant:"))
                );

        verify(sessions).history("sid-test", 50);
    }

    @Test
    void close_shouldDeleteSession() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/chat/close")
                        .queryParam("sessionId", "sid-test")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(sessions).delete("sid-test");
    }

    @Test
    void stream_shouldEmitChunks_andDone() {
        FluxExchangeResult<String> result = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/chat/stream")
                        .queryParam("question", "bonjour")
                        .queryParam("mode", "AUTO")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext("__SESSION__:sid-test")
                .expectNext("chunk-1")
                .expectNext("chunk-2")
                .expectNext("[DONE]")
                .verifyComplete();
    }

    private ArgumentMatcher<String> containsWelcomeText() {
        return s -> s != null && s.contains("Bienvenue sur EtudLife");
    }
}