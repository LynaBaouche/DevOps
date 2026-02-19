package com.etudlife.controller;

import com.etudlife.model.JobPreference;
import com.etudlife.model.Compte;
import com.etudlife.repository.JobPreferenceRepository;
import com.etudlife.repository.CompteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
public class JobPreferenceController {

    private final JobPreferenceRepository preferenceRepo;
    private final CompteRepository compteRepository;

    public JobPreferenceController(JobPreferenceRepository preferenceRepo, CompteRepository compteRepository) {
        this.preferenceRepo = preferenceRepo;
        this.compteRepository = compteRepository;
    }

    // Endpoint de test pour vérifier que le serveur Render est bien réveillé
    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Serveur réveillé et opérationnel !");
    }

    @PostMapping
    public ResponseEntity<?> savePreferences(@RequestBody JobPreference pref) {
        // Pour l'instant, on lie au premier compte trouvé (Mode démo)
        Compte compte = compteRepository.findAll().get(0);

        // On vérifie si une préférence existe déjà pour ce compte pour la mettre à jour
        JobPreference existing = preferenceRepo.findByCompte(compte);
        if (existing != null) {
            pref.setId(existing.getId()); // On garde le même ID pour écraser l'ancienne
        }

        pref.setCompte(compte);
        preferenceRepo.save(pref);

        return ResponseEntity.ok().build();
    }
}