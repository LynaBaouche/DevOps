package com.etudlife.repository;

import com.etudlife.model.CatalogueLivre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CatalogueRepository extends JpaRepository<CatalogueLivre, Long> {
    // Recherche dans le titre, l'auteur ou la catégorie
    List<CatalogueLivre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrCategorieContainingIgnoreCase(
            String titre, String auteur, String categorie);
}