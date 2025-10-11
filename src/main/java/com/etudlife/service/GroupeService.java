package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Groupe;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupeService {

    private final GroupeRepository groupeRepository;
    private final CompteRepository compteRepository;

    public GroupeService(GroupeRepository groupeRepository, CompteRepository compteRepository) {
        this.groupeRepository = groupeRepository;
        this.compteRepository = compteRepository;
    }

    // Créer un nouveau groupe
    public Groupe creerGroupe(String nom, String description) {
        Groupe groupe = new Groupe(nom, description);
        return groupeRepository.save(groupe);
    }

    // Lister tous les groupes
    public List<Groupe> getAllGroupes() {
        return groupeRepository.findAll();
    }

    // Ajouter un membre à un groupe
    public Optional<Groupe> ajouterMembre(Long groupeId, Long compteId) {
        Optional<Groupe> groupeOpt = groupeRepository.findById(groupeId);
        Optional<Compte> compteOpt = compteRepository.findById(compteId);

        if (groupeOpt.isPresent() && compteOpt.isPresent()) {
            Groupe groupe = groupeOpt.get();
            Compte compte = compteOpt.get();

            groupe.ajouterMembre(compte);
            groupeRepository.save(groupe);

            return Optional.of(groupe);
        }

        return Optional.empty();
    }
}
