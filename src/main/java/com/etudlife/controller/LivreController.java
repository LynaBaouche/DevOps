package com.etudlife.controller;

import com.etudlife.model.Livre;
import com.etudlife.repository.LivreRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/livre_bu")
@CrossOrigin(origins = "*")
public class LivreController {

    private final LivreRepository livreRepository;

    public LivreController(LivreRepository livreRepository) {
        this.livreRepository = livreRepository;
    }

    // Tous les livres (catalogue)
    @GetMapping
    public List<Livre> getAllLivres() {
        return livreRepository.findAll();
    }

    // Seulement les disponibles
    @GetMapping("/disponibles")
    public List<Livre> getLivresDisponibles() {
        return livreRepository.findByDisponibleTrue();
    }
}
