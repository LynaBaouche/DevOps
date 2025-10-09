package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.repository.CompteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompteService {

    @Autowired
    private CompteRepository compteRepository;

    public Compte creerCompte(Compte compte) {
        // On vérifie si un utilisateur avec le même nom/prénom n'existe pas déjà
        compteRepository.findByNomAndPrenom(compte.getNom(), compte.getPrenom())
                .ifPresent(c -> {
                    throw new IllegalStateException("Un compte avec ce nom et prénom existe déjà.");
                });
        return compteRepository.save(compte);
    }

    public Compte trouverCompteParNomEtPrenom(String nom, String prenom) {
        return compteRepository.findByNomAndPrenom(nom, prenom)
                .orElseThrow(() -> new EntityNotFoundException("Compte non trouvé."));
    }

    public List<Compte> listerComptes() {
        return compteRepository.findAll();
    }
}