package com.etudlife.service;

import com.etudlife.model.Livre;
import com.etudlife.model.Reservation;
import com.etudlife.model.ReservationBU;
import com.etudlife.repository.LivreRepository;
import com.etudlife.repository.ReservationBURepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class ReservationService {

    @Autowired
    private ReservationBURepository reservationRepo;

    @Autowired
    private LivreRepository livreRepo;

    public ReservationBU reserver(ReservationBU r) {

        Livre livre = livreRepo.findById(r.getIdLivre())
                .orElseThrow(() -> new IllegalArgumentException("Livre inconnu"));

        if (!livre.getDispo())
            throw new IllegalArgumentException("Livre déjà emprunté ou réservé.");

        livre.setDispo(false);
        livreRepo.save(livre);

        return reservationRepo.save(r);
    }
}
