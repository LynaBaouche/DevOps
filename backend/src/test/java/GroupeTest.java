
import com.etudlife.model.Compte;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import com.etudlife.repository.RecetteRepository;
import com.etudlife.service.CompteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//annonces
import com.etudlife.model.Annonce;
import com.etudlife.repository.AnnonceRepository;
import com.etudlife.service.AnnonceService;
import java.util.List;

//notifications
import com.etudlife.model.Notification;
import com.etudlife.model.NotificationType;
import com.etudlife.repository.NotificationRepository;
import com.etudlife.service.NotificationService;
// agenda
import com.etudlife.model.Evenement;
import com.etudlife.repository.EvenementRepository;
import com.etudlife.service.EvenementService;
import com.etudlife.service.LienService;
import com.etudlife.model.Compte;
import java.util.ArrayList;



@ExtendWith(MockitoExtension.class)
class GroupeTest {

    // PARTIE COMPTE

    @Mock
    CompteRepository compteRepository;

    @Mock
    GroupeRepository groupeRepository;

    @Mock
    RecetteRepository recetteRepository;

    @InjectMocks
    CompteService compteService;
    //annonces
    @Mock
    AnnonceRepository annonceRepository;

    @InjectMocks
    AnnonceService annonceService;

// notifications

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationService notificationService;   // pour tester NotificationService

// agenda

    @Mock
    EvenementRepository evenementRepository;

    @Mock
    LienService lienService;

    @Mock
    NotificationService notificationServiceForEvents;  // <--- nouveau mock

    @InjectMocks
    EvenementService evenementService;




    @Test
    void creerCompte_ok_quandEmailNouveau() {
        Compte c = new Compte("Kenza", "MENAD",
                "kenzamenad@parisnanterre.fr", "motdepasse123");

        when(compteRepository.findByEmail("kenzamenad@parisnanterre.fr"))
                .thenReturn(Optional.empty());
        when(compteRepository.save(any(Compte.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Compte created = compteService.creerCompte(c);

        assertNotNull(created);
        assertNotNull(created.getMotDePasse());
        assertNotEquals("motdepasse123", created.getMotDePasse());
        verify(compteRepository).save(created);
    }

    @Test
    void creerCompte_ko_emailDejaExistant() {
        Compte c = new Compte("Kenza", "Menad",
                "kenzamenad@parisnanterre.fr", "motdepasse123");

        // même email ici que dans le Compte c
        when(compteRepository.findByEmail("kenzamenad@parisnanterre.fr"))
                .thenReturn(Optional.of(new Compte()));

        assertThrows(IllegalStateException.class,
                () -> compteService.creerCompte(c));

        verify(compteRepository, never()).save(any());
    }


    // === CONNEXION ===

    @Test
    void login_ok() {
        Compte c = new Compte("Kenza", "Menad",
                "kenzamenad@parisnanterre.fr", "motdepasse123");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        c.setMotDePasse(encoder.encode("motdepasse123"));

        when(compteRepository.findByEmail("kenzamenad@parisnanterre.fr"))
                .thenReturn(Optional.of(c));
        when(compteRepository.save(any(Compte.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Compte logged = compteService.login(
                "kenzamenad@parisnanterre.fr", "motdepasse123");

        assertNotNull(logged.getLastConnection());
        verify(compteRepository).save(logged);
    }

    @Test
    void login_ko_mauvaisMotDePasse() {
        Compte c = new Compte("Kenza", "Menad",
                "kenzamenad@parisnanterre.fr", "motdepasse123");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        c.setMotDePasse(encoder.encode("motdepasse123"));

        when(compteRepository.findByEmail("kenzamenad@parisnanterre.fr"))
                .thenReturn(Optional.of(c));

        assertThrows(IllegalArgumentException.class,
                () -> compteService.login("kenzamenad@parisnanterre.fr", "wrong"));
    }

    @Test
    void login_ko_emailInconnu() {
        when(compteRepository.findByEmail("unknown@parisnanterre.fr"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> compteService.login("unknown@parisnanterre.fr", "x"));
    }

    // annonces:
    @Test
    void findAll_retourneListeAnnonces() {
        Annonce a1 = new Annonce();
        a1.setId(1L);
        a1.setTitre("Chambre à louer");

        Annonce a2 = new Annonce();
        a2.setId(2L);
        a2.setTitre("Cours de Java");

        when(annonceRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Annonce> result = annonceService.findAll();

        assertEquals(2, result.size());
        assertEquals("Chambre à louer", result.get(0).getTitre());
        verify(annonceRepository).findAll();
    }

    @Test
    void findByCategorie_filtreParCategorie() {
        Annonce a = new Annonce();
        a.setId(1L);
        a.setCategorie("logement");

        when(annonceRepository.findByCategorie("logement"))
                .thenReturn(List.of(a));

        List<Annonce> result = annonceService.findByCategorie("logement");

        assertEquals(1, result.size());
        assertEquals("logement", result.get(0).getCategorie());
        verify(annonceRepository).findByCategorie("logement");
    }

    @Test
    void findById_retourneAnnonceQuandExiste() {
        Annonce a = new Annonce();
        a.setId(10L);
        a.setTitre("Test");

        when(annonceRepository.findById(10L))
                .thenReturn(Optional.of(a));

        Annonce result = annonceService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(annonceRepository).findById(10L);
    }

    @Test
    void findById_retourneNullQuandAbsente() {
        when(annonceRepository.findById(99L))
                .thenReturn(Optional.empty());

        Annonce result = annonceService.findById(99L);

        assertNull(result);
        verify(annonceRepository).findById(99L);
    }

    @Test
    void findByUtilisateurId_retourneAnnoncesUtilisateur() {
        Annonce a = new Annonce();
        a.setId(1L);
        a.setUtilisateurId(5L);

        when(annonceRepository.findByUtilisateurId(5L))
                .thenReturn(List.of(a));

        List<Annonce> result = annonceService.findByUtilisateurId(5L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getUtilisateurId());
        verify(annonceRepository).findByUtilisateurId(5L);
    }

    @Test
    void save_persisteAnnonce() {
        Annonce a = new Annonce();
        a.setTitre("Nouvelle annonce");

        when(annonceRepository.save(a)).thenReturn(a);

        Annonce saved = annonceService.save(a);

        assertNotNull(saved);
        assertEquals("Nouvelle annonce", saved.getTitre());
        verify(annonceRepository).save(a);
    }

    @Test
    void delete_supprimeAnnonceParId() {
        Long id = 12L;

        annonceService.delete(id);

        verify(annonceRepository).deleteById(id);
    }

    //notifications
    // Création d'une notification
    @Test
    void createNotification_ok() {
        Notification n = new Notification(1L, NotificationType.ANNONCE,
                "Nouvelle annonce publiée", "/Annonce/annonces.html");

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification saved = invocation.getArgument(0);
                    saved.setRead(false);
                    return saved;
                });

        Notification created = notificationService.create(
                1L,
                NotificationType.ANNONCE,
                "Nouvelle annonce publiée",
                "/Annonce/annonces.html"
        );

        assertNotNull(created);
        assertEquals(1L, created.getUserId());
        assertEquals(NotificationType.ANNONCE, created.getType());
        assertFalse(created.isRead());
        verify(notificationRepository).save(any(Notification.class));
    }

    // Récupérer les notifications d'un utilisateur
    @Test
    void getForUser_retourneListeOrdonnée() {
        Notification n1 = new Notification(1L, NotificationType.NEW_MESSAGE,
                "Message 1", "/messages.html");
        Notification n2 = new Notification(1L, NotificationType.NEW_MESSAGE,
                "Message 2", "/messages.html");

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n1, n2));

        List<Notification> list = notificationService.getForUser(1L);

        assertEquals(2, list.size());
        assertEquals("Message 1", list.get(0).getMessage());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    // Marquer comme lue
    @Test
    void markAsRead_metNotificationEnLue() {
        Notification n = new Notification(1L, NotificationType.FRIEND_ADDED,
                "Nouvel ami", "/proches.html");
        // par défaut isRead = false

        when(notificationRepository.findById(10L))
                .thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.markAsRead(10L);

        assertTrue(n.isRead());
        verify(notificationRepository).findById(10L);
        verify(notificationRepository).save(n);
    }

    // Compter les non lues
    @Test
    void countUnread_retourneNombreNonLues() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L))
                .thenReturn(3L);

        long count = notificationService.countUnread(1L);

        assertEquals(3L, count);
        verify(notificationRepository).countByUserIdAndIsReadFalse(1L);
    }
    //agenda
// getByUserId délègue bien au repository
    @Test
    void getByUserId_retourneEvenementsUtilisateur() {
        Evenement e = new Evenement();
        e.setId(1L);

        when(evenementRepository.findByUtilisateurId(5L))
                .thenReturn(List.of(e));

        List<Evenement> result = evenementService.getByUserId(5L);

        assertEquals(1, result.size());
        verify(evenementRepository).findByUtilisateurId(5L);
    }

    @Test
    void add_sauvegardeEtCreeNotificationsPourProches() {
        // Prépare un utilisateur
        Compte user = new Compte("Kenza", "Menad",
                "kenza@parisnanterre.fr", "xxx");
        user.setId(10L);

        Evenement e = new Evenement();
        e.setTitre("Révision examen");
        e.setUtilisateur(user);

        // Simule des proches : 2 et 3
        when(lienService.getProcheIds(10L)).thenReturn(List.of(2L, 3L));

        // Simule la sauvegarde de l'événement
        when(evenementRepository.save(e)).thenReturn(e);

        Evenement saved = evenementService.add(e);

        assertNotNull(saved);
        verify(evenementRepository).save(e);

        // Vérifie que la notification est envoyée à chaque proche
        verify(notificationServiceForEvents).create(
                eq(2L),
                eq(NotificationType.NEW_EVENT),
                contains("Kenza Menad"),
                eq("/agenda.html")
        );
        verify(notificationServiceForEvents).create(
                eq(3L),
                eq(NotificationType.NEW_EVENT),
                contains("Kenza Menad"),
                eq("/agenda.html")
        );
    }

    // delete() délègue au repository
    @Test
    void delete_supprimeEvenementParId() {
        evenementService.delete(7L);

        verify(evenementRepository).deleteById(7L);
    }

    // getSharedAvailability récupère mes proches et moi-même
    @Test
    void getSharedAvailability_prendMesProchesEtMoi() {
        // mes proches
        when(lienService.getProcheIds(10L)).thenReturn(new ArrayList<>(List.of(2L, 3L)));

        List<Evenement> events = List.of(new Evenement());
        when(evenementRepository.findByUtilisateurIdIn(List.of(2L, 3L, 10L)))
                .thenReturn(events);

        List<Evenement> result = evenementService.getSharedAvailability(10L);

        assertEquals(1, result.size());
        verify(lienService).getProcheIds(10L);
        verify(evenementRepository).findByUtilisateurIdIn(List.of(2L, 3L, 10L));
    }

}
