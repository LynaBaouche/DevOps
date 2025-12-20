package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Groupe;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import com.etudlife.model.Recette;
import com.etudlife.repository.RecetteRepository;
import java.util.Set;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Service
public class CompteService {

    private final CompteRepository compteRepository;
    private final GroupeRepository groupeRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RecetteRepository recetteRepository;
    public CompteService(CompteRepository compteRepository, GroupeRepository groupeRepository, RecetteRepository recetteRepository) {
        this.compteRepository = compteRepository;
        this.groupeRepository = groupeRepository;
        this.recetteRepository = recetteRepository;
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
    // === GESTION DES FAVORIS ===

    public void ajouterFavori(Long compteId, Long recetteId) {
        Compte compte = lireCompteParId(compteId);
        Recette recette = recetteRepository.findById(recetteId)
                .orElseThrow(() -> new EntityNotFoundException("Recette introuvable"));

        compte.getRecettesFavorites().add(recette);
        compteRepository.save(compte);
    }
    public void retirerFavori(Long compteId, Long recetteId) {
        Compte compte = lireCompteParId(compteId);
        Recette recette = recetteRepository.findById(recetteId)
                .orElseThrow(() -> new EntityNotFoundException("Recette introuvable"));

        compte.getRecettesFavorites().remove(recette);
        compteRepository.save(compte);
    }

    public Set<Recette> listerFavoris(Long compteId) {
        Compte compte = lireCompteParId(compteId);
        return compte.getRecettesFavorites();
    }
}
