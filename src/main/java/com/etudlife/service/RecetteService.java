package com.etudlife.service;

import com.etudlife.model.Recette;
import com.etudlife.repository.RecetteRepository;
import org.springframework.stereotype.Service;
import com.etudlife.model.NotificationType;
import java.util.*;


@Service
public class RecetteService {

    private final RecetteRepository recetteRepository;
    private final NotificationService notificationService;

    public RecetteService(RecetteRepository recetteRepository,NotificationService notificationService) {
        this.recetteRepository = recetteRepository;
        this.notificationService = notificationService;
    }

    // Récupère toutes les recettes de la base
    public List<Recette> getAllRecettes() {
        return recetteRepository.findAll();
    }

    // Simule un menu hebdomadaire (Retourne une Map Jour -> {Midi: Recette, Soir: Recette})
    public Map<String, Map<String, Recette>> getMenuDeLaSemaine() {
        List<Recette> all = recetteRepository.findAll();

        // Si pas assez de recettes, on boucle ou on renvoie vide
        if (all.isEmpty()) return Collections.emptyMap();

        // On mélange pour varier chaque "semaine" (à chaque redémarrage ou appel)
        Collections.shuffle(all);

        Map<String, Map<String, Recette>> menuSemaine = new LinkedHashMap<>();
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

        int index = 0;
        for (String jour : jours) {
            Map<String, Recette> repasJour = new HashMap<>();

            // Recette Midi
            if (index < all.size()) repasJour.put("midi", all.get(index++));
            else index = 0; // On boucle si on n'a pas assez de recettes

            // Recette Soir
            if (index < all.size()) repasJour.put("soir", all.get(index++));
            else index = 0;

            menuSemaine.put(jour, repasJour);
        }

        return menuSemaine;
    }
    public Recette save(
            Recette recette,
            Long auteurId,
            String auteurNom,
            List<Long> prochesIds
    ) {
        // 1. Sauvegarder la recette
        Recette saved = recetteRepository.save(recette);


        return saved;
    }



}
