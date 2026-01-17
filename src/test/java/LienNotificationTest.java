import com.etudlife.EtudlifeApp;
import com.etudlife.model.Compte;
import com.etudlife.model.Notification;
import com.etudlife.model.NotificationType;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.LienRepository;
import com.etudlife.repository.NotificationRepository;
import com.etudlife.service.LienService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EtudlifeApp.class)
@Transactional
public class LienNotificationTest {

    @Autowired private LienService lienService;
    @Autowired private CompteRepository compteRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private LienRepository lienRepository;

    @Test
    void testCreationLienDeclencheNotification() {
        // 1. Setup
        Compte alice = compteRepository.save(new Compte("Alice", "A", "alice@fac.fr", "pw"));
        Compte bob = compteRepository.save(new Compte("Bob", "B", "bob@fac.fr", "pw"));

        // 2. Action : Alice ajoute Bob
        lienService.creerLien(alice.getId(), bob.getId());

        // 3. Verif Lien créé
        assertTrue(lienRepository.existsByCompteSourceIdAndCompteCibleId(alice.getId(), bob.getId()));

        // 4. VERIF NOTIFICATION (C'est le cœur du test)
        // On cherche les notifs de Bob
        List<Notification> notifsBob = notificationRepository.findByUserIdOrderByCreatedAtDesc(bob.getId());

        assertFalse(notifsBob.isEmpty(), "Bob aurait dû recevoir une notification");
        Notification notif = notifsBob.get(0);

        assertEquals(NotificationType.FRIEND_ADDED, notif.getType());
        assertTrue(notif.getMessage().contains("Alice"), "Le message doit mentionner Alice");
    }
}