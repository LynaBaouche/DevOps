import com.etudlife.repository.MessageRepository;
import com.etudlife.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void getExistingConv() {
        // GIVEN : Une conversation existe déjà (ID 500)
        Long user1 = 1L;
        Long user2 = 2L;
        when(messageRepository.findConversationIdByParticipants(user1, user2)).thenReturn(500L);

        // WHEN
        Long resultId = conversationService.getOrInitConversationId(user1, user2);

        // THEN
        assertEquals(500L, resultId);
    }

    @Test
    void initNewConv() {
        // GIVEN : Aucune conversation n'existe (null)
        Long user1 = 1L;
        Long user2 = 2L;
        when(messageRepository.findConversationIdByParticipants(user1, user2)).thenReturn(null);

        // WHEN
        Long resultId = conversationService.getOrInitConversationId(user1, user2);

        // THEN
        assertNotNull(resultId);
        // Vérifie qu'on a bien généré un ID (basé sur le temps dans ton code)
        assertTrue(resultId > 0);
    }
}