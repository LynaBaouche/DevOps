package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Lien;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.LienRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.etudlife.model.NotificationType;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LienService {

    private final LienRepository lienRepository;
    private final CompteRepository compteRepository;
    private final NotificationService notificationService;


    public LienService(LienRepository lienRepository, CompteRepository compteRepository,NotificationService notificationService) {
        this.lienRepository = lienRepository;
        this.compteRepository = compteRepository;
        this.notificationService = notificationService;
    }

    // Crée un lien entre deux comptes existants
    public Optional<Lien> creerLien(Long idSource, Long idCible) {
        // 1. On empêche de s'ajouter soi-même
        if (idSource.equals(idCible)) {
            return Optional.empty();
        }

        // 2. ON VÉRIFIE SI LE LIEN EXISTE DÉJÀ
        if (lienRepository.existsByCompteSourceIdAndCompteCibleId(idSource, idCible)) {
            return Optional.empty();
        }
        Optional<Compte> source = compteRepository.findById(idSource);
        Optional<Compte> cible = compteRepository.findById(idCible);

        if (source.isPresent() && cible.isPresent()) {

            Lien lien = new Lien(source.get(), cible.get());
            Lien saved = lienRepository.save(lien);

            // 🔔 notification
            notificationService.create(
                    cible.get().getId(),
                    NotificationType.FRIEND_ADDED,
                    source.get().getNom() + " vous a ajouté comme proche",
                    "/proches.html#mes-proches"
            );

            return Optional.of(saved);
        } else {
            return Optional.empty();
        }
    }


    // Liste les proches d’un compte
    public List<Lien> getLiensPourCompte(Long idSource) {
        Optional<Compte> compte = compteRepository.findById(idSource);
        return compte
                .map(c -> lienRepository.findByCompteSourceId(c.getId()))
                .orElse(List.of());
    }

    @Transactional
    public void supprimerLien(Long idSource, Long idCible) {
        // On supprime le lien dans le sens "Source -> Cible" (celui que je vois)
        lienRepository.deleteByCompteSourceIdAndCompteCibleId(idSource, idCible);
    }
    /*
    * Récupère la liste des IDs de tous les comptes que l'utilisateur a ajoutés en proche.
    */
    public List<Long> getProcheIds(Long idSource) {
        // La liste des liens (où l'utilisateur est la source)
        List<Lien> liens = getLiensPourCompte(idSource);

        // On retourne la liste des IDs cibles
        return liens.stream()
                .map(lien -> lien.getCompteCible().getId())
                .collect(Collectors.toList());
    }
}
