package com.etudlife.repository;


import com.etudlife.model.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    List<Annonce> findByCategorie(String categorie);
    List<Annonce> findByUtilisateurId(Long utilisateurId);



}


