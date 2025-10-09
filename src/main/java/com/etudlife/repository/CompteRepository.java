package com.etudlife.repository;

import com.etudlife.model.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {

    // Trouve un compte par la combinaison de son nom et prénom.
    Optional<Compte> findByNomAndPrenom(String nom, String prenom);
}