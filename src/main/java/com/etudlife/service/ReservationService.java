package com.etudlife.service;


import com.etudlife.model.LivreBu;
import com.etudlife.model.Reservation;
import com.etudlife.repository.ReservationRepository;
import com.etudlife.repository.LivreBuRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepo;
    @Autowired
    private LivreBuRepository livreRepo;

    @Transactional
    public String reserverLivre(Long livreId, Long userId) {
        // 1. Vérifier si le livre existe et est disponible
        LivreBu livre = livreRepo.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        if (!livre.isDisponible()) {
            return "Désolé, ce livre est déjà réservé.";
        }

        // 2. Créer la réservation
        Reservation res = new Reservation();
        res.setIdLivre(livreId);
        res.setIdUser(userId);
        res.setDateReservation(LocalDate.now());
        res.setEmpruntDomicile(true);

        // 3. Marquer le livre comme indisponible
        livre.setDisponible(false);

        livreRepo.save(livre);
        reservationRepo.save(res);

        return "Réservation confirmée";
    }

    public List<Reservation> getMesReservations(Long userId) {
        return reservationRepo.findByIdUser(userId);
    }

    public void reserverLivre(Long livreId, Long iduser, LocalDate date, boolean domicile) {
        // 1. Chercher le livre dans la table 'livre_bu'
        LivreBu livre = livreRepo.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre introuvable avec l'ID : " + livreId));

        // 2. Vérifier si le livre est déjà réservé (colonne 'disponible' à 0)
        if (!livre.isDisponible()) {
            throw new RuntimeException("Désolé, ce livre est déjà emprunté.");
        }

        // 3. Créer la nouvelle ligne pour la table 'reservation'
        Reservation nouvelleRes = new Reservation();
        nouvelleRes.setIdLivre(livreId);
        nouvelleRes.setIdUser(iduser);
        nouvelleRes.setDateReservation(LocalDate.now()); // Date d'aujourd'hui
        nouvelleRes.setDateRecuperation(date);           // Date choisie dans le calendrier du front
        nouvelleRes.setEmpruntDomicile(domicile);        // True ou False selon le bouton radio

        // 4. Enregistrer la réservation
        reservationRepo.save(nouvelleRes);

        // 5. METTRE À JOUR LE LIVRE : On change le bit 'disponible' de 1 à 0
        livre.setDisponible(false);
        livreRepo.save(livre);
    }

    public void annulerReservation(Long id) {
        // 1. Trouver la réservation pour savoir quel livre doit être libéré
        Reservation res = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable avec l'ID : " + id));

        // 2. Récupérer le livre associé à cette réservation dans la table livre_bu
        LivreBu livre = livreRepo.findById(res.getIdLivre())
                .orElseThrow(() -> new RuntimeException("Livre associé à la réservation introuvable"));

        // 3. Remettre le livre en "Disponible" (le bit repasse à 1)
        livre.setDisponible(true);
        livreRepo.save(livre);

        // 4. Supprimer définitivement la ligne de la table 'reservation'
        reservationRepo.delete(res);
    }
}