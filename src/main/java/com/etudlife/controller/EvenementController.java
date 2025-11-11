package com.etudlife.controller;

import com.etudlife.model.Evenement;
import com.etudlife.model.Compte;
import com.etudlife.repository.CompteRepository;
import com.etudlife.service.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;

    @Autowired
    private CompteRepository compteRepository;

    @GetMapping("/{userId}")
    public List<Evenement> getEvenements(@PathVariable Long userId) {
        return evenementService.getByUserId(userId);
    }

    @PostMapping("/{userId}")
    public Evenement ajouter(@PathVariable Long userId, @RequestBody Evenement evenement) {
        Compte utilisateur = compteRepository.findById(userId).orElseThrow();
        evenement.setUtilisateur(utilisateur);
        return evenementService.add(evenement);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        evenementService.delete(id);
    }
}
