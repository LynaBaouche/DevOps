package com.etudlife.repository;

import com.etudlife.model.EntreeMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EntreeMenuRepository extends JpaRepository<EntreeMenu, Long> {
    List<EntreeMenu> findByUtilisateurId(Long utilisateurId);
    void deleteByUtilisateurId(Long utilisateurId); // Pour vider le menu avant génération
}