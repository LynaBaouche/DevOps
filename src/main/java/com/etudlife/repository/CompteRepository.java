package com.etudlife.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.etudlife.model.Compte;
import java.util.List;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    List<Compte> findAllByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);

    Optional<Compte> findByEmail(String email); // <-- ajoute ceci
}

