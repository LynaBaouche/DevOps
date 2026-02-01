package DEVOPS1;

import com.etudlife.EtudlifeApp;
import com.etudlife.model.Compte;
import com.etudlife.model.Evenement;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.EvenementRepository;
import com.etudlife.service.EvenementService;
import com.etudlife.service.LienService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// On ajoute la propriété manquante directement ici pour que Spring puisse démarrer
@SpringBootTest(
        classes = EtudlifeApp.class,
        properties = {
                "RAPIDAPI_KEY=test_key_dummy",
                "OPENAI_API_KEY=test_key_dummy",
                "NAVITIA_TOKEN=test_key_dummy"
        }
)
@Transactional // Annule les modifications BDD après chaque test
public class AgendaIntegrationTest {

    @Autowired private EvenementService evenementService;
    @Autowired private LienService lienService;
    @Autowired private CompteRepository compteRepository;
    @Autowired private EvenementRepository evenementRepository;

    @Test
    void testVoirAgendaDesProches() {
        // 1. Création des utilisateurs
        Compte moi = compteRepository.save(new Compte("Moi", "Test", "moi@fac.fr", "123"));
        Compte ami = compteRepository.save(new Compte("Ami", "Proche", "ami@fac.fr", "123"));
        Compte inconnu = compteRepository.save(new Compte("Inconnu", "Autre", "inconnu@fac.fr", "123"));

        // 2. Création du lien d'amitié (Moi -> Ami)
        lienService.creerLien(moi.getId(), ami.getId());

        // 3. Création d'événements pour chacun
        createEvent(moi, "Mon Cours");
        createEvent(ami, "Cours Ami");
        createEvent(inconnu, "Cours Secret");

        // 4. ACTION : Récupérer l'agenda partagé pour 'Moi'
        List<Evenement> agendaPartage = evenementService.getSharedAvailability(moi.getId());

        // 5. VÉRIFICATIONS
        assertEquals(2, agendaPartage.size(), "Je devrais voir 2 événements (le mien et celui de mon ami)");

        boolean voisMonCours = agendaPartage.stream().anyMatch(e -> e.getTitre().equals("Mon Cours"));
        boolean voisCoursAmi = agendaPartage.stream().anyMatch(e -> e.getTitre().equals("Cours Ami"));
        boolean voisCoursInconnu = agendaPartage.stream().anyMatch(e -> e.getTitre().equals("Cours Secret"));

        assertTrue(voisMonCours, "Je dois voir mon propre événement");
        assertTrue(voisCoursAmi, "Je dois voir l'événement de mon ami");
        assertFalse(voisCoursInconnu, "Je NE dois PAS voir l'événement de l'inconnu");
    }

    private void createEvent(Compte user, String titre) {
        Evenement ev = new Evenement();
        ev.setTitre(titre);
        ev.setUtilisateur(user);
        ev.setDateDebut(LocalDateTime.now());
        ev.setDateFin(LocalDateTime.now().plusHours(1));
        evenementRepository.save(ev);
    }
}