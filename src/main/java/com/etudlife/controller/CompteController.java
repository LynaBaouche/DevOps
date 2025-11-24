package com.etudlife.controller;

import com.etudlife.model.Compte;
import com.etudlife.service.CompteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Compte getCompteParNomEtPrenom(
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
}
