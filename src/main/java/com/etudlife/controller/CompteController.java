package com.etudlife.controller;

import com.etudlife.model.Compte;
import com.etudlife.service.CompteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    @Autowired
    private CompteService compteService;

    // Pour créer un nouvel utilisateur
    @PostMapping
    public Compte creerCompte(@RequestBody Compte compte) {
        return compteService.creerCompte(compte);
    }

    // Pour "se connecter" ou chercher un utilisateur par nom/prénom
    @GetMapping("/search")
    public Compte trouverCompte(@RequestParam String nom, @RequestParam String prenom) {
        return compteService.trouverCompteParNomEtPrenom(nom, prenom);
    }

    // Pour voir tous les utilisateurs (utile pour le développement)
    @GetMapping
    public List<Compte> listerComptes() {
        return compteService.listerComptes();
    }
}