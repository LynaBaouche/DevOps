package DEVOPS1;

import com.etudlife.EtudlifeApp;
import com.etudlife.dto.ConversationPreviewDTO;
import com.etudlife.model.Compte;
import com.etudlife.model.Message;
import com.etudlife.repository.CompteRepository;
import com.etudlife.service.ConversationService;
import com.etudlife.service.LienService;
import com.etudlife.service.MessageService;
import com.etudlife.service.NotificationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = EtudlifeApp.class,
        properties = {
                "RAPIDAPI_KEY=test_key_dummy",
                "OPENAI_API_KEY=test_key_dummy",
                "NAVITIA_TOKEN=test_key_dummy"
        }
)
@Transactional
public class SocialIntegrationTest {

    @Autowired private LienService lienService;
    @Autowired private MessageService messageService;
    @Autowired private ConversationService conversationService;

    // On a besoin du repository pour créer de vrais utilisateurs en BDD
    @Autowired private CompteRepository compteRepository;

    // On mock les notifications pour ne pas spammer ou avoir d'erreurs externes
    @MockBean private NotificationService notificationService;

    @Test
    void scenario_AjoutAmi_Puis_Discussion() {
        // =================================================================
        // ÉTAPE 1 : CRÉATION DE DONNÉES DE TEST (Utilisateurs Alice et Bob)
        // =================================================================
        Compte alice = new Compte();
        alice.setPrenom("Alice"); alice.setNom("Test"); alice.setEmail("alice@parisnanterre.fr");
        alice = compteRepository.save(alice);

        Compte bob = new Compte();
        bob.setPrenom("Bob"); bob.setNom("Test"); bob.setEmail("bob@parisnanterre.fr");
        bob = compteRepository.save(bob);

        // =================================================================
        // ÉTAPE 2 : LOGIQUE "PROCHES" (LienService)
        // =================================================================
        // Alice ajoute Bob en ami
        lienService.creerLien(alice.getId(), bob.getId());

        // VÉRIFICATION : Est-ce que Bob est bien dans la liste des proches d'Alice ?
        List<Long> prochesAlice = lienService.getProcheIds(alice.getId());
        assertTrue(prochesAlice.contains(bob.getId()), "Bob doit être dans les proches d'Alice");

        // =================================================================
        // ÉTAPE 3 : LOGIQUE "INIT CONVERSATION" (ConversationService)
        // =================================================================
        // Le système doit générer ou trouver un ID de conversation entre eux
        Long convId = conversationService.getOrInitConversationId(alice.getId(), bob.getId());

        assertNotNull(convId, "L'ID de conversation ne doit pas être null");

        // =================================================================
        // ÉTAPE 4 : LOGIQUE "MESSAGERIE" (MessageService)
        // =================================================================
        // Alice envoie un message à Bob
        String contenuMsg = "Salut Bob, on révise à la BU ?";
        messageService.saveNewMessage(convId, alice.getId(), bob.getId(), contenuMsg);

        // VÉRIFICATION A : Le message est-il bien sauvegardé ?
        List<Message> historique = messageService.getLatestMessages(convId);
        assertEquals(1, historique.size());
        assertEquals(contenuMsg, historique.get(0).getContent());

        // VÉRIFICATION B (CRITIQUE) : Le SQL complexe de ConversationPreview fonctionne-t-il ?
        // Alice rafraîchit sa liste de conversations. Elle doit voir Bob.
        List<ConversationPreviewDTO> previews = conversationService.getPreviewsByUserId(alice.getId());

        assertFalse(previews.isEmpty(), "Alice devrait avoir une conversation active");
        ConversationPreviewDTO previewBob = previews.get(0);

        assertEquals(bob.getId(), previewBob.getContactId()); // C'est bien Bob
        assertEquals("Bob Test", previewBob.getContactName()); // Le nom est bon
        assertEquals(contenuMsg, previewBob.getLastMessageContent()); // Le dernier message est bon
    }
}