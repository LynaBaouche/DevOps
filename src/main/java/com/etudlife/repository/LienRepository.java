package com.etudlife.repository;

import com.etudlife.model.Lien;
import com.etudlife.model.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LienRepository extends JpaRepository<Lien, Long> {
    List<Lien> findByCompteSource(Compte compteSource);
}
