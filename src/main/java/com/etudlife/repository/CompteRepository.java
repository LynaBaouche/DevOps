package com.etudlife.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.etudlife.model.Compte;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    Optional<Compte> findByNomAndPrenom(String nom, String prenom);
    Optional<Compte> findByEmail(String email); // <-- ajoute ceci
}

