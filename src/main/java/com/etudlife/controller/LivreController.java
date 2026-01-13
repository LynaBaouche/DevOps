package com.etudlife.controller;

import com.etudlife.model.LivreBu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}