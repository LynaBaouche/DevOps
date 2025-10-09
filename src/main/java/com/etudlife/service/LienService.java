package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Lien;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.LienRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LienService {

    private final LienRepository lienRepository;
    private final CompteRepository compteRepository;

    public LienService(LienRepository lienRepository, CompteRepository compteRepository) {
        this.lienRepository = lienRepository;
        this.compteRepository = compteRepository;
    }

    // Crée un lien entre deux comptes existants
    public Optional<Lien> creerLien(Long idSource, Long idCible) {
        Optional<Compte> source = compteRepository.findById(idSource);
        Optional<Compte> cible = compteRepository.findById(idCible);

        if (source.isPresent() && cible.isPresent()) {
            Lien lien = new Lien(source.get(), cible.get());
            return Optional.of(lienRepository.save(lien));
        } else {
            return Optional.empty();
        }
    }

    // Liste les proches d’un compte
    public List<Lien> getLiensPourCompte(Long idSource) {
        Optional<Compte> compte = compteRepository.findById(idSource);
        return compte.map(lienRepository::findByCompteSource).orElse(List.of());
    }

    public void supprimerLien(Long id) {
        lienRepository.deleteById(id);
    }
}
