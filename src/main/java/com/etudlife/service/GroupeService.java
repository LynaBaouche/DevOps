package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Groupe;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- 1. AJOUTE CET IMPORT

import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
public class GroupeService {

    @Autowired
    private GroupeRepository groupeRepository;

    @Autowired
    private CompteRepository compteRepository;

    public GroupeService(GroupeRepository groupeRepository, CompteRepository compteRepository) {
        this.groupeRepository = groupeRepository;
        this.compteRepository = compteRepository;
    }

    // Créer un nouveau groupe
    public Groupe creerGroupe(String nom, String description, String categorie) {
        Groupe groupe = new Groupe(nom, description, categorie);
        return groupeRepository.save(groupe);
    }

    // Lister tous les groupes
    public List<Groupe> getAllGroupes() {
        return groupeRepository.findAll();
    }
    // ✅ ALGORITHME DE RECOMMANDATION
    public List<Groupe> getRecommandations(Long userId) {
        Compte user = compteRepository.findById(userId).orElseThrow();
        Set<String> userHobbies = user.getHobbies();

        List<Groupe> allGroupes = groupeRepository.findAll();

        return allGroupes.stream()
                // 1. Filtrer : On garde seulement si la catégorie du groupe est dans les hobbies du user
                .filter(g -> g.getCategorie() != null && userHobbies.contains(g.getCategorie()))
                // 2. Filtrer : On ne recommande pas les groupes où je suis déjà membre
                .filter(g -> g.getMembres().stream().noneMatch(m -> m.getId().equals(userId)))
                .collect(Collectors.toList());
    }
    // Ajouter un membre à un groupe
    @Transactional // <-- 2. AJOUTE CETTE ANNOTATION
    public Optional<Groupe> ajouterMembre(Long groupeId, Long compteId) {
        Optional<Groupe> groupeOpt = groupeRepository.findById(groupeId);
        Optional<Compte> compteOpt = compteRepository.findById(compteId);

        if (groupeOpt.isPresent() && compteOpt.isPresent()) {
            Groupe groupe = groupeOpt.get();
            Compte compte = compteOpt.get();

            groupe.ajouterMembre(compte); // Cette ligne fonctionnera maintenant
            groupeRepository.save(groupe);

            return Optional.of(groupe);
        }

        return Optional.empty();
    }

}