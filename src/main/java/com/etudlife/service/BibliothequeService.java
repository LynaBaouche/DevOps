package com.etudlife.service;

import com.etudlife.model.Reservation;
import com.etudlife.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BibliothequeService {

    private final ReservationRepository reservationRepository;

    public Reservation reserver(
            Long userId,
            Long livreId,
            LocalDate dateRecup,
            boolean domicile
    ) {
        Reservation r = new Reservation();
        r.setIdUser(userId);
        r.setIdLivre(livreId);
        r.setDateReservation(LocalDate.now());
        r.setDateRecuperation(dateRecup);
        r.setEmpruntDomicile(domicile);

        return reservationRepository.save(r);
    }

    public List<Reservation> getReservationsUser(Long userId) {
        return reservationRepository.findByIdUser(userId);
    }
}
