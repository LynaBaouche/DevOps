package com.etudlife.controller;

import com.etudlife.model.Evenement;
import com.etudlife.model.Compte;
import com.etudlife.model.Lien;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.EvenementRepository;
import com.etudlife.service.EvenementService;
import com.etudlife.service.LienService; // Assurez-vous d'avoir accès au service ou repo Lien
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private EvenementService evenementService;

    @Autowired
    private LienService lienService; // Pour récupérer les proches

    // Récupérer mes événements uniquement
    @GetMapping("/{userId}")
    public List<Evenement> getEvenements(@PathVariable Long userId) {
        return evenementRepository.findByUtilisateurId(userId);
    }

    @GetMapping("/shared/{userId}")
    public List<Evenement> getSharedEvents(@PathVariable Long userId) {
        // 1. Récupérer tous les événements (les miens + ceux des proches)
        List<Evenement> allEvents = evenementService.getSharedAvailability(userId);

        // 2. Filtrer et sécuriser les données pour l'affichage
        return allEvents.stream().map(event -> {
            // Si l'événement n'appartient pas à l'utilisateur connecté
            if (!event.getUtilisateur().getId().equals(userId)) {
                // On crée une COPIE de l'objet pour l'affichage (pour ne pas modifier la BDD)
                Evenement safeEvent = new Evenement();
                safeEvent.setId(event.getId());
                safeEvent.setDateDebut(event.getDateDebut());
                safeEvent.setDateFin(event.getDateFin());

                // 🔒 PROTECTION DE LA VIE PRIVÉE
                safeEvent.setTitre("Occupé");
                safeEvent.setDescription(null);

                // On garde l'utilisateur pour savoir quelle couleur afficher
                safeEvent.setUtilisateur(event.getUtilisateur());
                return safeEvent;
            }
            // Si c'est mon événement, je le retourne tel quel
            return event;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{userId}")
    public Evenement ajouter(@PathVariable Long userId, @RequestBody Evenement evenement) {
        Compte utilisateur = compteRepository.findById(userId).orElseThrow();
        evenement.setUtilisateur(utilisateur);

        // Couleur par défaut si non fournie
        if (evenement.getCouleur() == null || evenement.getCouleur().isEmpty()) {
            evenement.setCouleur("#3788d8"); // Bleu standard
        }

        return evenementRepository.save(evenement);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        evenementRepository.deleteById(id);
    }
}