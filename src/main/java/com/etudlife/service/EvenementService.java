package com.etudlife.service;

import com.etudlife.model.Evenement;
import com.etudlife.repository.EvenementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EvenementService {


    @Autowired
    private EvenementRepository evenementRepository;
    @Autowired
    private LienService lienService;

    public List<Evenement> getByUserId(Long id) {
        return evenementRepository.findByUtilisateurId(id);
    }

    public Evenement add(Evenement e) {
        return evenementRepository.save(e);
    }

    public void delete(Long id) {
        evenementRepository.deleteById(id);
    }

    public List<Evenement> getSharedAvailability(Long myUserId) {
        // 1. Récupère les IDs des proches
        List<Long> procheIds = lienService.getProcheIds(myUserId);

        // 2. Ajoute l'ID de l'utilisateur courant à la liste des IDs à chercher
        procheIds.add(myUserId);
        // 3. Récupère TOUS les événements pour cette liste d'IDs
        return evenementRepository.findByUtilisateurIdIn(procheIds);
    }
}
