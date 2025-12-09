package com.etudlife.controller;

import com.etudlife.model.Recette;
import com.etudlife.service.RecetteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recettes")
@CrossOrigin(origins = "*")
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
}