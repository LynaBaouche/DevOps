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
    public Compte creerCompte(@RequestBody Compte compte) {
        return compteService.creerCompte(compte);
    }

    // === LISTER LES COMPTES ===
    @GetMapping
    public List<Compte> getAllComptes() {
        return compteService.listerComptes();
    }

    // === CHERCHER PAR NOM ET PRÉNOM (ancien login)
    @GetMapping("/search")
    public Compte getCompteParNomEtPrenom(
            @RequestParam String nom,
            @RequestParam String prenom
    ) {
        return compteService.trouverCompteParNomEtPrenom(nom, prenom);
    }

    // === LOGIN PAR EMAIL ET MOT DE PASSE ===
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String motDePasse = credentials.get("password");

        try {
            Compte compte = compteService.login(email, motDePasse);
            return ResponseEntity.ok(compte);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }
    // 🔹 Récupérer un compte par son ID (avec ses groupes)
    @GetMapping("/{id}")
    public Compte getCompteById(@PathVariable Long id) {
        return compteService.lireCompteParId(id);
    }

}
