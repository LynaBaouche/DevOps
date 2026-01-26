package com.etudlife.controller;

import com.etudlife.model.Compte;
import com.etudlife.model.Recette;
import com.etudlife.repository.CompteRepository;
import com.etudlife.service.CompteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/comptes")
@CrossOrigin(origins = "*")
public class CompteController {

    private final CompteService compteService;
    private final CompteRepository compteRepository;

    public CompteController(CompteService compteService,
                            CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
        this.compteService = compteService;
    }

    // === INSCRIPTION ===
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> creerCompte(@RequestBody Compte compte) {
        try {
            Compte created = compteService.creerCompte(compte);
            return ResponseEntity.ok(created);

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409
                    .body("Un compte existe déjà avec cette adresse email.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // === LOGIN PAR EMAIL & MOT DE PASSE ===
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        try {
            Compte compte = compteService.login(email, password);
            return ResponseEntity.ok(compte);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Mot de passe incorrect.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Aucun compte trouvé avec cet email.");
        }
    }

    // === Mettre à jour les hobbies ===
    @PutMapping("/{id}/hobbies")
    public ResponseEntity<?> updateHobbies(@PathVariable Long id,
                                           @RequestBody Set<String> hobbies) {
        Compte compte = compteService.lireCompteParId(id);
        if (compte == null) return ResponseEntity.notFound().build();

        compte.setHobbies(hobbies);
        compteRepository.save(compte);
        return ResponseEntity.ok().build();
    }

    // === LISTER LES COMPTES ===
    @GetMapping
    public List<Compte> getAllComptes() {
        return compteService.listerComptes();
    }

    // === CHERCHER PAR NOM / PRÉNOM ===
    @GetMapping("/search")
    public List<Compte> getCompteParNomEtPrenom(
            @RequestParam String nom,
            @RequestParam String prenom
    ) {
        return compteService.trouverCompteParNomEtPrenom(nom, prenom);
    }

    // === GET PAR ID ===
    @GetMapping("/{id}")
    public Compte getCompteById(@PathVariable Long id) {
        return compteService.lireCompteParId(id);
    }

    // === FAVORIS RECETTES ===
    @PostMapping("/{id}/favoris/{recetteId}")
    public ResponseEntity<Void> ajouterFavori(@PathVariable Long id,
                                              @PathVariable Long recetteId) {
        compteService.ajouterFavori(id, recetteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/favoris/{recetteId}")
    public ResponseEntity<Void> retirerFavori(@PathVariable Long id,
                                              @PathVariable Long recetteId) {
        compteService.retirerFavori(id, recetteId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/favoris")
    public ResponseEntity<Set<Recette>> listerFavoris(@PathVariable Long id) {
        return ResponseEntity.ok(compteService.listerFavoris(id));
    }

    // === 1. PING (POUR DIRE "JE SUIS LÀ") ===
    @PostMapping("/ping")
    public ResponseEntity<Void> signalPresence(@RequestHeader("X-User-ID") Long userId) {
        if (userId != null) {
            compteService.updatePresence(userId);
        }
        return ResponseEntity.ok().build();
    }

    // === 2. VERIFIER LE STATUT D'UN AUTRE ===
    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getCompteStatus(@PathVariable Long id) {

        boolean isOnline = compteService.isUserOnline(id);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "online", isOnline,
                "status", isOnline ? "ONLINE" : "OFFLINE"
        ));
    }

    // === MISE À JOUR DU PROFIL ===
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfil(@PathVariable Long id,
                                          @RequestBody Compte payload) {
        try {
            Compte compte = compteService.lireCompteParId(id);

            // Mettre à jour uniquement les champs modifiables
            compte.setPrenom(payload.getPrenom());
            compte.setNom(payload.getNom());
            compte.setEmail(payload.getEmail());
            compte.setTelephone(payload.getTelephone());
            compte.setAdresse(payload.getAdresse());
            compte.setBiographie(payload.getBiographie());

            Compte saved = compteRepository.save(compte);
            return ResponseEntity.ok(saved);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Compte introuvable");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }
}
