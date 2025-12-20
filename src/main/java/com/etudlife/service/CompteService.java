package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Groupe;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Service
public class CompteService {

    private final CompteRepository compteRepository;
    private final GroupeRepository groupeRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CompteService(CompteRepository compteRepository, GroupeRepository groupeRepository) {
        this.compteRepository = compteRepository;
        this.groupeRepository = groupeRepository;
    }

    // === INSCRIPTION ===
    public Compte creerCompte(Compte compte) {

        if (compte.getEmail() != null &&
                compteRepository.findByEmail(compte.getEmail()).isPresent()) {
            throw new IllegalStateException("Un compte avec cet email existe déjà.");
        }

        // 🔐 Hash du mot de passe
        String motDePasseHash = passwordEncoder.encode(compte.getMotDePasse());
        compte.setMotDePasse(motDePasseHash);

        return compteRepository.save(compte);
    }

    // === CONNEXION ===
    public Compte login(String email, String motDePasse) {

        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Aucun compte trouvé avec cet email."));

        if (compte.getMotDePasse() == null ||
                !passwordEncoder.matches(motDePasse, compte.getMotDePasse())) {
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
