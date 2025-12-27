package com.etudlife.service;

import com.etudlife.model.Evenement;
import com.etudlife.model.NotificationType;
import com.etudlife.repository.EvenementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    // ✅ AJOUT (notifications)
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LienService lienService;

    public List<Evenement> getByUserId(Long id) {
        return evenementRepository.findByUtilisateurId(id);
    }

    public Evenement add(Evenement e) {

        Evenement saved = evenementRepository.save(e);

        Long userId = e.getUtilisateur().getId();
        String auteurNom = e.getUtilisateur().getPrenom() + " " + e.getUtilisateur().getNom();

        List<Long> procheIds = lienService.getProcheIds(userId);

        for (Long procheId : procheIds) {
            notificationService.create(
                    procheId,
                    NotificationType.NEW_EVENT,
                    auteurNom + " a ajouté un nouvel événement",
                    "/agenda.html"
            );
        }

        return saved;
    }


    public void delete(Long id) {
        evenementRepository.deleteById(id);
    }

    public List<Evenement> getSharedAvailability(Long myUserId) {

        // 🔹 code EXISTANT (inchangé)
        List<Long> procheIds = lienService.getProcheIds(myUserId);
        procheIds.add(myUserId);

        return evenementRepository.findByUtilisateurIdIn(procheIds);
    }
}
