package com.etudlife.controller;

import com.etudlife.model.Groupe;
import com.etudlife.service.GroupeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groupes")
@CrossOrigin(origins = "*")
public class GroupeController {
    @Autowired
    private final GroupeService groupeService;

    public GroupeController(GroupeService groupeService) {
        this.groupeService = groupeService;
    }

    //  Créer un groupe
    @PostMapping(consumes = "application/json", produces = "application/json")
    public Groupe creerGroupe(@RequestBody Groupe groupe) {
        return groupeService.creerGroupe(groupe.getNom(), groupe.getDescription(), groupe.getCategorie());
    }
    // lister les recommandations de groupes pour un utilisateur
    @GetMapping("/recommandations/{userId}")
    public List<Groupe> getRecommandations(@PathVariable Long userId) {
        return groupeService.getRecommandations(userId);
    }
    //  Lister tous les groupes
    @GetMapping(produces = "application/json")
    public List<Groupe> getAllGroupes() {
        return groupeService.getAllGroupes();
    }

    //  Ajouter un membre à un groupe
    @PostMapping("/{groupeId}/ajouter/{compteId}")
    public Groupe ajouterMembre(
            @PathVariable Long groupeId,
            @PathVariable Long compteId
    ) {
        Optional<Groupe> groupeOpt = groupeService.ajouterMembre(groupeId, compteId);
        return groupeOpt.orElseThrow(() -> new RuntimeException("Groupe ou compte introuvable"));
    }
}
