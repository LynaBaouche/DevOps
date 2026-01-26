package com.etudlife.repository;

import com.etudlife.model.CatalogueLivre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CatalogueRepository extends JpaRepository<CatalogueLivre, Long> {

    // Cette méthode magique de Spring Data JPA va créer la requête SQL toute seule !
    List<CatalogueLivre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrCategorieContainingIgnoreCase(
            String titre, String auteur, String categorie);
}