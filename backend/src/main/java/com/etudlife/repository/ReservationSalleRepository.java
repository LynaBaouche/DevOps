package com.etudlife.repository;

import com.etudlife.model.ReservationSalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationSalleRepository extends JpaRepository<ReservationSalle, Long> {

    // Cette méthode permettra de récupérer uniquement les réservations d'un étudiant précis
    List<ReservationSalle> findByIdUser(Long idUser);
}