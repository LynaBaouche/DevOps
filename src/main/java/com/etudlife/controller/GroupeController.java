package com.etudlife.controller;

import com.etudlife.model.Groupe;
import com.etudlife.service.GroupeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groupes")
public class GroupeController {

    private final GroupeService groupeService;

    public GroupeController(GroupeService groupeService) {
        this.groupeService = groupeService;
    }

    @PostMapping
    public Groupe creerGroupe(@RequestParam String nom, @RequestParam String description) {
        return groupeService.creerGroupe(nom, description);
    }

    @GetMapping
    public List<Groupe> getAll() {
        return groupeService.getAllGroupes();
    }

    @PostMapping("/{groupeId}/ajouterMembre/{compteId}")
    public Optional<Groupe> ajouterMembre(@PathVariable Long groupeId, @PathVariable Long compteId) {
        return groupeService.ajouterMembre(groupeId, compteId);
    }
}
