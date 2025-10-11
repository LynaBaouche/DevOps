package com.etudlife.controller;

import com.etudlife.model.Compte;
import com.etudlife.service.CompteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comptes")
@CrossOrigin(origins = "*") // pour autoriser Postman / front local
public class CompteController {

    private final CompteService compteService;

    public CompteController(CompteService compteService) {
        this.compteService = compteService;
    }

    // Créer un compte
    @PostMapping(consumes = "application/json", produces = "application/json")
    public Compte creerCompte(@RequestBody Compte compte) {
        return compteService.creerCompte(compte);
    }

    // Lister tous les comptes
    @GetMapping
    public List<Compte> getAllComptes() {
        return compteService.listerComptes();
    }

    // Chercher un compte par nom et prénom
    @GetMapping("/search")
    public Compte getCompteParNomEtPrenom(
            @RequestParam String nom,
            @RequestParam String prenom
    ) {
        return compteService.trouverCompteParNomEtPrenom(nom, prenom);
    }
}
