package com.etudlife.repository;
import com.etudlife.model.ReservationBU;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationBURepository extends JpaRepository<ReservationBU, Long> {
    List<ReservationBU> findByIdUser(Long idUser);

}
