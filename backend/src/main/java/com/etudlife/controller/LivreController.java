package com.etudlife.controller;

import com.etudlife.model.CatalogueLivre;
import com.etudlife.model.LivreBu;
import com.etudlife.repository.CatalogueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.etudlife.repository.LivreBuRepository;

import java.util.List;

@RestController
@RequestMapping("/api/livres") // Ton front appellera cette route
@CrossOrigin
public class LivreController {

    @Autowired
    private LivreBuRepository repository;

    @GetMapping
    public List<LivreBu> getAllLivres() {
        // Cette méthode va chercher les données dans 'livre_bu' via le Repository
        return repository.findAll();
    }
    @Autowired
    private CatalogueRepository catalogueRepository;

    // Route appelée par la barre de recherche du catalogue.html
    @GetMapping("/search")
    public List<CatalogueLivre> search(@RequestParam(required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return catalogueRepository.findAll(); // Affiche tout par défaut
        }
        // Cherche dans les 50+ livres du catalogue_general
        return catalogueRepository.findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrCategorieContainingIgnoreCase(
                query, query, query);
    }


    @GetMapping("/search-global") // <-- Vérifie que c'est bien "search-global"
    public List<CatalogueLivre> searchInGlobalCatalogue(@RequestParam String query) {
        return catalogueRepository.findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrCategorieContainingIgnoreCase(
                query, query, query);
    }
}