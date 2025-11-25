package com.etudlife.controller;


import com.etudlife.model.Annonce;
import com.etudlife.service.AnnonceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin(origins = "*")
public class AnnonceController {

    private final AnnonceService service;

    public AnnonceController(AnnonceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Annonce> getAll(@RequestParam(required = false) String categorie) {
        if (categorie == null || categorie.equals("toutes")) {
            return service.findAll();
        }
        return service.findByCategorie(categorie);
    }
}
