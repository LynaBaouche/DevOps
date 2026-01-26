package com.etudlife;

import com.etudlife.model.*;
import com.etudlife.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import static org.hamcrest.Matchers.is;
import com.etudlife.service.NotificationService;


@SpringBootTest(classes = EtudlifeApp.class)
@AutoConfigureMockMvc
class GroupTestIntegration {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired CompteRepository compteRepository;
    @Autowired AnnonceRepository annonceRepository;
    @Autowired LienRepository lienRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired EvenementRepository   evenementRepository;
    @Autowired  NotificationService notificationService;
    // =====================
    // UTILS
    // =====================
    private String uniqueEmail(String base) {
        return base.replace("@", "+" + System.nanoTime() + "@");
    }


    // COMPTE : INSCRIPTION

    @Test
    void inscription_creeCompte_enBase() throws Exception {
        String email = uniqueEmail("kenza@parisnanterre.fr");

        String body = """
        {
          "prenom": "Kenza",
          "nom": "Menad",
          "email": "%s",
          "motDePasse": "motdepasse123"
        }
        """.formatted(email);

        String json = mockMvc.perform(post("/api/comptes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn().getResponse().getContentAsString();

        Compte created = objectMapper.readValue(json, Compte.class);
        Compte enBase = compteRepository.findById(created.getId()).orElse(null);

        assertNotNull(enBase);
        assertNotEquals("motdepasse123", enBase.getMotDePasse());
    }

    // COMPTE : UPDATE PROFIL

    @Test
    void updateProfil_modifieLesChamps() throws Exception {
        Compte c = new Compte(
                "Kenza",
                "Menad",
                uniqueEmail("kenza@parisnanterre.fr"),
                "pass"
        );
        c = compteRepository.save(c);

        String body = """
        {
          "prenom": "Kenza",
          "nom": "Menad",
          "email": "%s",
          "telephone": "0612345678",
          "adresse": "Rue de Paris",
          "biographie": "Étudiante MIAGE"
        }
        """.formatted(uniqueEmail("kenza.updated@parisnanterre.fr"));

        mockMvc.perform(put("/api/comptes/" + c.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telephone").value("0612345678"))
                .andExpect(jsonPath("$.biographie").value("Étudiante MIAGE"));
    }


    // ANNONCE + NOTIFICATIONS

    @Test
    void createAnnonce_creeAnnonceEtNotifications() throws Exception {

        Compte auteur = compteRepository.save(
                new Compte("Kenza", "Menad", uniqueEmail("kenza@etu.fr"), "x"));

        Compte proche1 = compteRepository.save(
                new Compte("Alice", "Durand", uniqueEmail("alice@etu.fr"), "x"));

        Compte proche2 = compteRepository.save(
                new Compte("Bob", "Martin", uniqueEmail("bob@etu.fr"), "x"));

        Lien l1 = new Lien();
        l1.setCompteSource(auteur);
        l1.setCompteCible(proche1);
        lienRepository.save(l1);

        Lien l2 = new Lien();
        l2.setCompteSource(auteur);
        l2.setCompteCible(proche2);
        lienRepository.save(l2);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "photo.jpg",
                "image/jpeg",
                "fake".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/annonces")
                        .file(image)
                        .param("titre", "Chambre à louer")
                        .param("categorie", "logement")
                        .param("prix", "450")
                        .param("ville", "Nanterre")
                        .param("description", "Campus proche")
                        .param("auteur", auteur.getPrenom() + " " + auteur.getNom()) // ✅ OBLIGATOIRE
                        .param("utilisateurId", auteur.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titre").value("Chambre à louer"));

        List<Notification> notifs = notificationRepository.findAll();
        assertThat(notifs.stream()
                .filter(n -> n.getType() == NotificationType.ANNONCE))
                .hasSizeGreaterThanOrEqualTo(2);
    }

    // ANNONCES : FILTRE

    @Test
    void getAll_filtreParCategorie() throws Exception {

        Annonce a1 = new Annonce();
        a1.setTitre("Logement 1");
        a1.setCategorie("logement");
        annonceRepository.save(a1);

        Annonce a2 = new Annonce();
        a2.setTitre("Cours Java");
        a2.setCategorie("cours");
        annonceRepository.save(a2);

        mockMvc.perform(get("/api/annonces")
                        .param("categorie", "logement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.titre==\"Logement 1\")]").exists());
    }

    // ANNONCES : PAR UTILISATEUR

    @Test
    void getByUtilisateur_retourneSesAnnonces() throws Exception {

        Annonce a = new Annonce();
        a.setTitre("Annonce user");
        a.setUtilisateurId(10L);
        annonceRepository.save(a);

        mockMvc.perform(get("/api/annonces/utilisateur/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Annonce user"));
    }

    // ANNONCES : VUES

    @Test
    void incrementVue_incrementerCompteur() throws Exception {

        Annonce a = new Annonce();
        a.setTitre("Vue test");
        a.setVues(0);
        a = annonceRepository.save(a);

        mockMvc.perform(put("/api/annonces/" + a.getId() + "/vue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vues").value(1));
    }

    // ANNONCES : SUPPRESSION

    @Test
    void delete_supprimeAnnonce() throws Exception {

        Annonce a = new Annonce();
        a.setTitre("À supprimer");
        a = annonceRepository.save(a);

        mockMvc.perform(delete("/api/annonces/" + a.getId()))
                .andExpect(status().isOk());

        assertTrue(annonceRepository.findById(a.getId()).isEmpty());
    }


    //agenda
    @Test
    void agenda_ajouterEvenement_creeEvenementEtNotifications() throws Exception {

        Compte auteur = compteRepository.save(
                new Compte("Kenza", "Menad", uniqueEmail("kenza@etu.fr"), "x")
        );

        Compte proche = compteRepository.save(
                new Compte("Alice", "Durand", uniqueEmail("alice@etu.fr"), "x")
        );

        // lien auteur → proche
        Lien lien = new Lien();
        lien.setCompteSource(auteur);
        lien.setCompteCible(proche);
        lienRepository.save(lien);

        String body = """
    {
      "titre": "Révision Java",
      "description": "Avant l'examen",
      "dateDebut": "%s",
      "dateFin": "%s"
    }
    """.formatted(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/api/evenements/" + auteur.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titre").value("Révision Java"))
                .andExpect(jsonPath("$.couleur").value("#3788d8")); // couleur par défaut

        // 🔔 notification envoyée au proche
        List<Notification> notifs = notificationRepository.findAll();
        assertThat(notifs.stream()
                .anyMatch(n -> n.getType() == NotificationType.NEW_EVENT))
                .isTrue();
    }

    @Test
    void agenda_getEvenements_retourneMesEvenements() throws Exception {

        Compte user = compteRepository.save(
                new Compte("User", "Agenda", uniqueEmail("user@etu.fr"), "x")
        );

        Evenement e = new Evenement();
        e.setTitre("Cours MIAGE");
        e.setDateDebut(LocalDateTime.now());
        e.setDateFin(LocalDateTime.now().plusHours(1));
        e.setUtilisateur(user);
        evenementRepository.save(e);

        mockMvc.perform(get("/api/evenements/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Cours MIAGE"));
    }

    @Test
    void agenda_getSharedEvents_masqueEvenementsDesProches() throws Exception {

        Compte moi = compteRepository.save(
                new Compte("Moi", "User", uniqueEmail("moi@etu.fr"), "x")
        );

        Compte proche = compteRepository.save(
                new Compte("Alice", "Durand", uniqueEmail("alice@etu.fr"), "x")
        );

        Lien lien = new Lien();
        lien.setCompteSource(moi);
        lien.setCompteCible(proche);
        lienRepository.save(lien);

        Evenement eProche = new Evenement();
        eProche.setTitre("RDV Médical");
        eProche.setDescription("Privé");
        eProche.setDateDebut(LocalDateTime.now());
        eProche.setDateFin(LocalDateTime.now().plusHours(1));
        eProche.setUtilisateur(proche);
        evenementRepository.save(eProche);

        mockMvc.perform(get("/api/evenements/shared/" + moi.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Occupé"))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }
    @Test
    void agenda_supprimerEvenement_supprimeEnBase() throws Exception {

        Compte user = compteRepository.save(
                new Compte("Delete", "User", uniqueEmail("delete@etu.fr"), "x")
        );

        Evenement e = new Evenement();
        e.setTitre("À supprimer");
        e.setDateDebut(LocalDateTime.now());
        e.setDateFin(LocalDateTime.now().plusHours(1));
        e.setUtilisateur(user);
        e = evenementRepository.save(e);

        mockMvc.perform(delete("/api/evenements/" + e.getId()))
                .andExpect(status().isNoContent());

        assertTrue(evenementRepository.findById(e.getId()).isEmpty());
    }
//notifications
@Test
void notification_getNotifications_retourneListe() throws Exception {

    Long userId = 100L;

    notificationService.create(
            userId,
            NotificationType.ANNONCE,
            "Nouvelle annonce publiée",
            "/annonces.html"
    );

    notificationService.create(
            userId,
            NotificationType.NEW_EVENT,
            "Nouvel événement ajouté",
            "/agenda.html"
    );

    mockMvc.perform(get("/api/notifications/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(2)))
            .andExpect(jsonPath("$[0].message").exists());
}
//compter les notif non lues
@Test
void notification_unreadCount_retourneNombreCorrect() throws Exception {

    Long userId = 200L;

    notificationService.create(
            userId,
            NotificationType.NEW_EVENT,
            "Event 1",
            "/agenda.html"
    );
    notificationService.create(
            userId,
            NotificationType.ANNONCE,
            "Annonce 1",
            "/annonces.html"
    );

    mockMvc.perform(get("/api/notifications/" + userId + "/unread-count"))
            .andExpect(status().isOk())
            .andExpect(content().string("2"));
}
//Marquer une notif comme lue
@Test
void notification_markAsRead_marqueCommeLue() throws Exception {

    Long userId = 300L;

    Notification notif = notificationService.create(
            userId,
            NotificationType.NEW_MESSAGE,
            "Nouveau message",
            "/messages.html"
    );

    // marquer comme lue
    mockMvc.perform(put("/api/notifications/" + notif.getId() + "/read"))
            .andExpect(status().isOk());

    // compteur doit être 0
    mockMvc.perform(get("/api/notifications/" + userId + "/unread-count"))
            .andExpect(status().isOk())
            .andExpect(content().string("0"));
}
//ordonner les notif par date
@Test
void notification_getNotifications_ordreDescendant() throws Exception {

    Long userId = 400L;

    notificationService.create(
            userId,
            NotificationType.NEW_EVENT,
            "Ancienne notif",
            null
    );

    Thread.sleep(10); // garantir ordre createdAt

    notificationService.create(
            userId,
            NotificationType.ANNONCE,
            "Nouvelle notif",
            null
    );

    mockMvc.perform(get("/api/notifications/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].message").value("Nouvelle notif"));
}

}
