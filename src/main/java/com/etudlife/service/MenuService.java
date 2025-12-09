package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.EntreeMenu;
import com.etudlife.model.Recette;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.EntreeMenuRepository;
import com.etudlife.repository.RecetteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MenuService {

    private final EntreeMenuRepository menuRepository;
    private final RecetteRepository recetteRepository;
    private final CompteRepository compteRepository;

    public MenuService(EntreeMenuRepository menuRepository, RecetteRepository recetteRepository, CompteRepository compteRepository) {
        this.menuRepository = menuRepository;
        this.recetteRepository = recetteRepository;
        this.compteRepository = compteRepository;
    }

    public List<EntreeMenu> getMenuUtilisateur(Long userId) {
        return menuRepository.findByUtilisateurId(userId);
    }

    @Transactional
    public List<EntreeMenu> genererMenuAutomatique(Long userId, Double budgetMax) {
        Compte user = compteRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur inconnu"));

        // 1. Vider le menu existant
        menuRepository.deleteByUtilisateurId(userId);

        // 2. Récupérer toutes les recettes et mélanger
        List<Recette> recettes = recetteRepository.findAll();
        Collections.shuffle(recettes);

        List<EntreeMenu> nouveauMenu = new ArrayList<>();
        Double coutTotal = 0.0;
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String[] moments = {"Midi", "Soir"};

        int index = 0;

        // 3. Remplir la semaine
        for (String jour : jours) {
            for (String moment : moments) {
                if (index >= recettes.size()) index = 0; // Si pas assez de recettes, on boucle

                Recette r = recettes.get(index);

                // Si on a encore du budget ou qu'on est au début
                if (coutTotal + r.getPrixEstime() <= budgetMax) {
                    nouveauMenu.add(new EntreeMenu(jour, moment, r, user));
                    coutTotal += r.getPrixEstime();
                }
                index++;
            }
        }

        return menuRepository.saveAll(nouveauMenu);
    }
}