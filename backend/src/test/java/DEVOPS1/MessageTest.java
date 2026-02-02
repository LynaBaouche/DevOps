package DEVOPS1;

import com.etudlife.model.Message;
import com.etudlife.model.NotificationType;
import com.etudlife.repository.MessageRepository;
import com.etudlife.service.MessageService;
import com.etudlife.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active Mockito
class MessageTest {

    @Mock
    private MessageRepository messageRepository; // Simule la BDD

    @Mock
    private NotificationService notificationService; // Simule les notifs

    @InjectMocks
    private MessageService messageService; // Service à tester

    @Test
    void saveMessage() {
        // GIVEN
        Long convId = 1L;
        Long senderId = 10L;
        Long receiverId = 20L;
        String content = "Salut !";

        // Simulation : quand on sauvegarde, on renvoie le message
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Message result = messageService.saveNewMessage(convId, senderId, receiverId, content);

        // THEN
        assertNotNull(result.getTimestamp()); // Vérifie que la date est mise
        assertEquals(content, result.getContent());

        // Vérifie que la notification est partie
        verify(notificationService).create(eq(receiverId), eq(NotificationType.NEW_MESSAGE), anyString(), anyString());
    }

    @Test
    void getHistory() {
        // GIVEN
        List<Message> mockList = new ArrayList<>();
        mockList.add(new Message());

        when(messageRepository.findTop50ByConversationIdOrderByTimestampDesc(1L)).thenReturn(mockList);

        // WHEN
        List<Message> result = messageService.getLatestMessages(1L);

        // THEN
        assertEquals(1, result.size());
        verify(messageRepository).findTop50ByConversationIdOrderByTimestampDesc(1L);
    }

    @Test
    void deleteSuccess() {
        // GIVEN
        Long msgId = 5L;
        Long userId = 10L; // C'est lui l'auteur

        Message msg = new Message();
        msg.setId(msgId);
        msg.setSenderId(userId); // L'auteur correspond

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));

        // WHEN
        messageService.deleteMessage(msgId, userId);

        // THEN
        verify(messageRepository).delete(msg); // Vérifie que delete a été appelé
    }

    @Test
    void deleteForbidden() {
        // GIVEN
        Long msgId = 5L;
        Long userId = 99L; // Ce n'est PAS l'auteur

        Message msg = new Message();
        msg.setId(msgId);
        msg.setSenderId(10L); // L'auteur est l'ID 10

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));

        // WHEN & THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.deleteMessage(msgId, userId);
        });

        assertEquals("Vous n'avez pas le droit de supprimer ce message.", exception.getMessage());
        verify(messageRepository, never()).delete(any()); // Vérifie que delete n'a JAMAIS été appelé
    }
}