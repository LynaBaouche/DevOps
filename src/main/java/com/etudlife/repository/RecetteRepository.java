package com.etudlife.repository;

import com.etudlife.model.Recette;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecetteRepository extends JpaRepository<Recette, Long> {
    List<Recette> findByPrixEstimeLessThanEqual(Double prixMax);
}