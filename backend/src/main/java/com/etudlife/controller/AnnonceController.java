package com.etudlife.controller;

import com.etudlife.model.Annonce;
import com.etudlife.model.Compte;
import com.etudlife.model.Lien;
import com.etudlife.model.NotificationType;
import com.etudlife.repository.LienRepository;
import com.etudlife.service.AnnonceService;
import com.etudlife.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin(origins = "*")
public class AnnonceController {

    private final AnnonceService service;
    private final LienRepository lienRepository;
    private final NotificationService notificationService;

    public AnnonceController(AnnonceService service,
                             LienRepository lienRepository,
                             NotificationService notificationService) {
        this.service = service;
        this.lienRepository = lienRepository;
        this.notificationService = notificationService;
    }

    // 🔵 Récupérer toutes les annonces
    @GetMapping
    public List<Annonce> getAll(@RequestParam(required = false) String categorie) {
        if (categorie == null || categorie.equals("toutes")) {
            return service.findAll();
        }
        return service.findByCategorie(categorie);
    }

    // 🔵 Récupérer UNE annonce
    @GetMapping("/{id}")
    public Annonce getById(@PathVariable Long id) {
        return service.findById(id);
    }

    // 🔵 Récupérer annonces par utilisateur
    @GetMapping("/utilisateur/{userId}")
    public List<Annonce> getByUser(@PathVariable Long userId) {
        return service.findByUtilisateurId(userId);
    }

    // 🔴 Supprimer une annonce
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // 👁️ Incrémenter les vues
    @PutMapping("/{id}/vue")
    public Annonce incrementVue(@PathVariable Long id) {
        Annonce a = service.findById(id);
        if (a == null) return null;

        a.setVues(a.getVues() + 1);
        return service.save(a);
    }

    // 🟢 CRÉER une annonce (multipart) + notifier les proches
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Annonce create(
            @RequestParam String titre,
            @RequestParam String categorie,
            @RequestParam String prix,
            @RequestParam String ville,
            @RequestParam String description,
            @RequestParam String auteur,
            @RequestParam Long utilisateurId,
            @RequestParam(required = false) String lien,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        Annonce annonce = new Annonce();
        annonce.setTitre(titre);
        annonce.setCategorie(categorie);
        annonce.setPrix(prix);
        annonce.setVille(ville);
        annonce.setDescription(description);
        annonce.setAuteur(auteur);
        annonce.setUtilisateurId(utilisateurId);
        annonce.setLien(lien);
        annonce.setDatePublication(LocalDate.now().toString());
        annonce.setVues(0);

        // 🔁 stocker l'image directement en base (base64)
        if (image != null && !image.isEmpty()) {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            annonce.setImage(base64);
        } else {
            annonce.setImage(null);
        }

        Annonce saved = service.save(annonce);

        // 🔔 NOTIFICATIONS POUR LES PROCHES
        if (utilisateurId != null) {
            try {
                List<Lien> liens = lienRepository.findByCompteSourceId(utilisateurId);

                String message = auteur + " a publié une nouvelle annonce.";
                String linkNotif = "/Annonce/annonces.html";

                for (Lien lienProche : liens) {
                    Compte proche = lienProche.getCompteCible();
                    if (proche != null && proche.getId() != null) {
                        notificationService.create(
                                proche.getId(),
                                NotificationType.ANNONCE,
                                message,
                                linkNotif
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la création des notifications d'annonce : " + e.getMessage());
                e.printStackTrace();
            }
        }

        return saved;
    }

    // 🟡 MODIFIER une annonce (multipart)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Annonce update(
            @PathVariable Long id,
            @RequestParam String titre,
            @RequestParam String categorie,
            @RequestParam String prix,
            @RequestParam String ville,
            @RequestParam String description,
            @RequestParam(required = false) String lien,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {

        Annonce a = service.findById(id);
        if (a == null) return null;

        a.setTitre(titre);
        a.setCategorie(categorie);
        a.setPrix(prix);
        a.setVille(ville);
        a.setDescription(description);
        a.setLien(lien);

        // si nouvelle image → on remplace (toujours en base64)
        if (image != null && !image.isEmpty()) {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            a.setImage(base64);
        }

        return service.save(a);
    }
}
