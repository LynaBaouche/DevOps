package com.etudlife.controller;

import com.etudlife.model.Recette;
import com.etudlife.service.RecetteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recettes")
public class RecetteController {

    private final RecetteService service;

    public RecetteController(RecetteService service) {
        this.service = service;
    }

    // Récupère le menu tout prêt pour la semaine
    @GetMapping("/semaine")
    public ResponseEntity<Map<String, Map<String, Recette>>> getMenuSemaine() {
        return ResponseEntity.ok(service.getMenuDeLaSemaine());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Recette> getRecetteById(@PathVariable Long id) {
        try {
            Recette recette = service.getRecetteById(id);
            return ResponseEntity.ok(recette);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // Renvoie 404 si pas trouvé
        }
    }
}