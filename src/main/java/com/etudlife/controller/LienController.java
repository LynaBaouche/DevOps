package com.etudlife.controller;

import com.etudlife.model.Lien;
import com.etudlife.service.LienService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/liens")
public class LienController {

    private final LienService lienService;

    public LienController(LienService lienService) {
        this.lienService = lienService;
    }

    @PostMapping
    public Optional<Lien> creerLien(@RequestParam Long idSource, @RequestParam Long idCible) {
        return lienService.creerLien(idSource, idCible);
    }

    @GetMapping("/{idSource}/proches")
    public List<Lien> getProches(@PathVariable Long idSource) {
        return lienService.getLiensPourCompte(idSource);
    }

    @DeleteMapping("/{id}")
    public void supprimerLien(@PathVariable Long id) {
        lienService.supprimerLien(id);
    }
}
