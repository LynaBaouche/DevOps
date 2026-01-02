package com.etudlife.repository;

import com.etudlife.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByIdUser(Long idUser);
}
