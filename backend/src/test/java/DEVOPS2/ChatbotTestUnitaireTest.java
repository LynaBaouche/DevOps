package DEVOPS2;

import com.etudlife.controller.ChatController;
import com.etudlife.controller.ChatStreamController;
import com.etudlife.dto.ChatResponse;
import com.etudlife.service.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChatbotTestUnitaireTest {

    /* ----------------------------
       Helper reflection
     ---------------------------- */
    private static void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    /* ----------------------------
       1) ChatService (logique)
     ---------------------------- */
    @Nested
    @ExtendWith(MockitoExtension.class)
    class ChatServiceTests {

        @Mock PdfKnowledgeBase kb;
        @Mock GeminiClient gemini;
        @Mock ChatSessionService sessions;
        @Mock SavedJobService savedJobService;

        ChatService service;

        @BeforeEach
        void setup() {
            // constructeur a SavedJobService en double => on passe le même mock aux 2
            service = new ChatService(kb, gemini, sessions, savedJobService, savedJobService);
        }

        @Test
        void merci_shouldReturnPolite_andNoGeminiNoKb() {
            ChatResponse res = service.ask("s1", "merci", "AUTO");

            assertTrue(res.isSuccess());
            assertEquals("none", res.getSource());
            assertTrue(res.getResponse().contains("Pas de souci"));

            verify(sessions).append("s1", "user", "merci");
            verify(sessions).append(eq("s1"), eq("assistant"), contains("Pas de souci"));
            verifyNoInteractions(gemini);
            verifyNoInteractions(kb);
        }

        @Test
        void bonjour_shouldReturnPresentation_andNoGeminiNoKb() {
            ChatResponse res = service.ask("s1", "bonjour", "AUTO");

            assertTrue(res.getResponse().contains("Bienvenue sur EtudLife"));
            assertEquals("none", res.getSource());

            verifyNoInteractions(gemini);
            verifyNoInteractions(kb);
        }

        @Test
        void autoMode_siteQuestion_shouldSearchSITE() {
            when(sessions.history("s1", 10)).thenReturn(List.of());
            when(kb.search(eq("Comment publier une annonce ?"), eq("SITE")))
                    .thenReturn(List.of(new PdfKnowledgeBase.Chunk("site.pdf", "texte", "SITE")));
            when(gemini.generate(anyString())).thenReturn("OK");

            ChatResponse res = service.ask("s1", "Comment publier une annonce ?", "AUTO");

            assertEquals("OK", res.getResponse());
            assertTrue(res.getSource().contains("site.pdf"));
            verify(kb).search("Comment publier une annonce ?", "SITE");
            verify(gemini).generate(contains("CONTEXTE:"));
        }

        @Test
        void fallback_otherMode_whenFirstEmpty() {
            when(sessions.history("s1", 10)).thenReturn(List.of());
            when(kb.search("profil", "SITE")).thenReturn(List.of());
            when(kb.search("profil", "REGLEMENT"))
                    .thenReturn(List.of(new PdfKnowledgeBase.Chunk("reg.pdf", "ok", "REGLEMENT")));
            when(gemini.generate(anyString())).thenReturn("fallback");

            ChatResponse res = service.ask("s1", "profil", "AUTO");

            assertEquals("fallback", res.getResponse());
            verify(kb).search("profil", "SITE");
            verify(kb).search("profil", "REGLEMENT");
        }

        @Test
        void noHits_bothModes_shouldReturnSafe_andNoGemini() {
            when(sessions.history("s1", 10)).thenReturn(List.of());
            when(kb.search("inconnu", "REGLEMENT")).thenReturn(List.of());
            when(kb.search("inconnu", "SITE")).thenReturn(List.of());

            ChatResponse res = service.ask("s1", "inconnu", "AUTO");

            assertEquals("Désolé, je n’ai pas d’information sur ce sujet.", res.getResponse());
            assertEquals("none", res.getSource());
            verifyNoInteractions(gemini);
        }
    }

    /* ----------------------------
       2) ChatSessionService (Redis)
     ---------------------------- */
    @Nested
    @ExtendWith(MockitoExtension.class)
    class ChatSessionServiceTests {

        @Mock StringRedisTemplate redis;
        @Mock ListOperations<String, String> listOps;

        ChatSessionService sessions;

        @BeforeEach
        void setup() {
            // lenient => évite UnnecessaryStubbingException quand un test n’utilise pas opsForList()
            lenient().when(redis.opsForList()).thenReturn(listOps);
            sessions = new ChatSessionService(redis);
        }

        @Test
        void newSession_shouldCreateKey_pushPop_andExpire() {
            String sid = sessions.newSession();
            assertNotNull(sid);
            assertFalse(sid.isBlank());

            verify(listOps).rightPush(startsWith("chat:session:"), eq(""));
            verify(listOps).leftPop(startsWith("chat:session:"));
            verify(redis).expire(startsWith("chat:session:"), any(Duration.class));
        }

        @Test
        void append_shouldPushLine_andRefreshTTL() {
            sessions.append("S1", "user", "hello");

            verify(listOps).rightPush("chat:session:S1", "user:hello");
            verify(redis).expire(eq("chat:session:S1"), any(Duration.class));
        }

        @Test
        void history_whenEmpty_shouldReturnEmptyList() {
            when(listOps.size("chat:session:S1")).thenReturn(0L);

            List<String> h = sessions.history("S1", 50);
            assertTrue(h.isEmpty());
        }

        @Test
        void history_shouldReturnRangeLastN() {
            when(listOps.size("chat:session:S1")).thenReturn(3L);
            when(listOps.range("chat:session:S1", 0, -1)).thenReturn(List.of("a", "b", "c"));

            List<String> h = sessions.history("S1", 50);
            assertEquals(3, h.size());
            assertEquals("a", h.get(0));
        }

        @Test
        void delete_shouldCallRedisDelete() {
            sessions.delete("S1");
            verify(redis).delete("chat:session:S1");
        }
    }

    /* ----------------------------
       3) ChatStreamService (Flux + chunk)
     ---------------------------- */
    @Nested
    @ExtendWith(MockitoExtension.class)
    class ChatStreamServiceTests {

        @Mock ChatService chatService;
        @Mock ChatSessionService sessions;

        @Test
        void streamAnswer_shouldCreateSessionIfMissing_andEmitSessionHeader() {
            when(sessions.newSession()).thenReturn("S_NEW");
            when(chatService.ask(eq("S_NEW"), eq("q"), any()))
                    .thenReturn(new ChatResponse(true, "S_NEW", "q", "un deux trois quatre", "none"));

            ChatStreamService svc = new ChatStreamService(chatService, sessions);

            StepVerifier.create(svc.streamAnswer(null, "q", "AUTO").take(1))
                    .expectNext("__SESSION__:S_NEW")
                    .verifyComplete();
        }
    }

    /* ----------------------------
       4) PdfKnowledgeBase (search)
     ---------------------------- */
    @Nested
    class PdfKnowledgeBaseTests {

        @Test
        void search_shouldFilterByModeAndScore() throws Exception {
            PdfKnowledgeBase kb = new PdfKnowledgeBase();
            setField(kb, "maxChunks", 4); // IMPORTANT : sinon limit(0) => liste vide

            Field chunksField = PdfKnowledgeBase.class.getDeclaredField("chunks");
            chunksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<PdfKnowledgeBase.Chunk> chunks = (List<PdfKnowledgeBase.Chunk>) chunksField.get(kb);

            chunks.add(new PdfKnowledgeBase.Chunk("site.pdf", "publier annonce etudlife", "SITE"));
            chunks.add(new PdfKnowledgeBase.Chunk("reg.pdf", "reglement examen fraude", "REGLEMENT"));

            List<PdfKnowledgeBase.Chunk> res1 = kb.search("publier annonce", "SITE");
            assertFalse(res1.isEmpty());
            assertEquals("site.pdf", res1.get(0).source());

            List<PdfKnowledgeBase.Chunk> res2 = kb.search("fraude examen", "REGLEMENT");
            assertFalse(res2.isEmpty());
            assertEquals("reg.pdf", res2.get(0).source());
        }

        @Test
        void search_invalidMode_shouldBehaveAsAUTO() throws Exception {
            PdfKnowledgeBase kb = new PdfKnowledgeBase();
            setField(kb, "maxChunks", 4);

            Field chunksField = PdfKnowledgeBase.class.getDeclaredField("chunks");
            chunksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<PdfKnowledgeBase.Chunk> chunks = (List<PdfKnowledgeBase.Chunk>) chunksField.get(kb);

            chunks.add(new PdfKnowledgeBase.Chunk("a.pdf", "agenda proches evenement", "SITE"));
            chunks.add(new PdfKnowledgeBase.Chunk("b.pdf", "reglement interieur", "REGLEMENT"));

            assertFalse(kb.search("agenda", "XYZ").isEmpty());
            assertFalse(kb.search("reglement", "???").isEmpty());
        }
    }

    /* ----------------------------
       5) GeminiClient (minimum utile)
     ---------------------------- */
    @Nested
    class GeminiClientTests {

        @Test
        void generate_whenApiKeyMissing_returnsMessage() throws Exception {
            GeminiClient client = new GeminiClient(org.springframework.web.reactive.function.client.WebClient.builder());

            Field apiKeyField = GeminiClient.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(client, "");

            String res = client.generate("test");
            assertTrue(res.toLowerCase().contains("clé"));
            assertTrue(res.toLowerCase().contains("manquante"));
        }
    }

    /* ----------------------------
       6) ChatController (HTTP MockMvc)
     ---------------------------- */
    @Nested
    @ExtendWith(MockitoExtension.class)
    class ChatControllerTests {

        @Mock ChatService chatService;
        @Mock ChatSessionService sessions;

        MockMvc mvc;

        @BeforeEach
        void setup() {
            ChatController controller = new ChatController(chatService, sessions);
            mvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

        @Test
        void newSession_shouldReturnSessionId() throws Exception {
            when(sessions.newSession()).thenReturn("SID123");

            mvc.perform(post("/api/chat/new-session"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("SID123"));
        }

        @Test
        void message_missingQuestion_shouldReturn400() throws Exception {
            mvc.perform(post("/api/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void message_whenNoSessionId_shouldCreateNewSession() throws Exception {
            when(sessions.newSession()).thenReturn("SNEW");
            when(chatService.ask(eq("SNEW"), eq("hello"), any()))
                    .thenReturn(new ChatResponse(true, "SNEW", "hello", "ok", "none"));

            mvc.perform(post("/api/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"question\":\"hello\",\"mode\":\"AUTO\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("SNEW"))
                    .andExpect(jsonPath("$.response").value("ok"));
        }

        @Test
        void history_shouldReturnMessages() throws Exception {
            when(sessions.history("S1", 50)).thenReturn(List.of("user:hi", "assistant:yo"));

            mvc.perform(get("/api/chat/history").param("sessionId", "S1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("S1"))
                    .andExpect(jsonPath("$.messages[0]").value("user:hi"));
        }

        @Test
        void close_shouldDeleteSession() throws Exception {
            mvc.perform(post("/api/chat/close").param("sessionId", "S1"))
                    .andExpect(status().isOk());

            verify(sessions).delete("S1");
        }
    }

    /* ----------------------------
       7) ChatStreamController (SSE)
     ---------------------------- */
    @Nested
    @ExtendWith(MockitoExtension.class)
    class ChatStreamControllerTests {

        @Mock ChatStreamService chatStreamService;

        @Test
        void stream_shouldEmitChunks_andDoneEvent() {
            when(chatStreamService.streamAnswer(any(), eq("q"), any()))
                    .thenReturn(Flux.just("__SESSION__:S1", "un", "deux"));

            ChatStreamController controller = new ChatStreamController(chatStreamService);
            WebTestClient client = WebTestClient.bindToController(controller).build();

            client.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/chat/stream")
                            .queryParam("question", "q")
                            .queryParam("sessionId", "S1")
                            .queryParam("mode", "AUTO")
                            .build())
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                    .expectBody(String.class)
                    .consumeWith(body -> {
                        String txt = body.getResponseBody();
                        assertNotNull(txt);
                        assertTrue(txt.contains("__SESSION__:S1"));
                        assertTrue(txt.contains("un"));
                        assertTrue(txt.contains("deux"));
                        assertTrue(txt.contains("[DONE]"));
                    });
        }
    }
}
