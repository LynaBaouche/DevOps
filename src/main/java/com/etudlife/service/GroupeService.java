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

    public Groupe creerGroupe(String nom, String description) {
        Groupe g = new Groupe(nom, description);
        return groupeRepository.save(g);
    }

    public List<Groupe> getAllGroupes() {
        return groupeRepository.findAll();
    }

    public Optional<Groupe> getById(Long id) {
        return groupeRepository.findById(id);
    }

    public Optional<Groupe> ajouterMembre(Long groupeId, Long compteId) {
        Optional<Groupe> groupe = groupeRepository.findById(groupeId);
        Optional<Compte> compte = compteRepository.findById(compteId);

        if (groupe.isPresent() && compte.isPresent()) {
            Groupe g = groupe.get();
            g.ajouterMembre(compte.get());
            return Optional.of(groupeRepository.save(g));
        }
        return Optional.empty();
    }
}
