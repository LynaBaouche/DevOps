package com.etudlife.controller;

import com.etudlife.model.Compte;
import com.etudlife.service.CompteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.etudlife.model.Recette;
import java.util.Set;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comptes")
@CrossOrigin(origins = "*")
public class CompteController {

    private final CompteService compteService;

    public CompteController(CompteService compteService) {
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
    @PostMapping("/{id}/favoris/{recetteId}")
    public ResponseEntity<Void> ajouterFavori(@PathVariable Long id, @PathVariable Long recetteId) {
        compteService.ajouterFavori(id, recetteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/favoris/{recetteId}")
    public ResponseEntity<Void> retirerFavori(@PathVariable Long id, @PathVariable Long recetteId) {
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
}
