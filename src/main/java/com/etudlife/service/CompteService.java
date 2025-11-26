package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Groupe;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompteService {

    private final CompteRepository compteRepository;
    private final GroupeRepository groupeRepository;

    public CompteService(CompteRepository compteRepository, GroupeRepository groupeRepository) {
        this.compteRepository = compteRepository;
        this.groupeRepository = groupeRepository;
    }

    // === INSCRIPTION ===
    public Compte creerCompte(Compte compte) {
        // Vérifie que l'email n’existe pas déjà
        if (compte.getEmail() != null && compteRepository.findByEmail(compte.getEmail()).isPresent()) {
            throw new IllegalStateException("Un compte avec cet email existe déjà.");
        }

        // (Facultatif) Ajouter à un groupe par défaut
        // Groupe g1 = groupeRepository.findById(1L).orElse(null);
        // if (g1 != null) {
        //     compte.getGroupes().add(g1);
        //     g1.getMembres().add(compte);
        // }

        return compteRepository.save(compte);
    }

    // === CONNEXION ===
    public Compte login(String email, String motDePasse) {
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Aucun compte trouvé avec cet email."));

        if (compte.getMotDePasse() == null || !compte.getMotDePasse().equals(motDePasse)) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        return compte;
    }

    // === RECHERCHE PAR NOM ET PRÉNOM ===
    public List<Compte> trouverCompteParNomEtPrenom(String nom, String prenom) {
     return compteRepository.findAllByNomIgnoreCaseAndPrenomIgnoreCase(nom, prenom);
    }

    // === LISTE DES COMPTES ===
    public List<Compte> listerComptes() {
        return compteRepository.findAll();
    }
    public Compte lireCompteParId(Long id) {
        return compteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compte introuvable"));
    }

}
