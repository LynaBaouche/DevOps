package com.etudlife.repository;

import com.etudlife.model.Lien;
import com.etudlife.model.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface LienRepository extends JpaRepository<Lien, Long> {
    // ancien : List<Lien> findByCompteSource(Compte compteSource);

    List<Lien> findByCompteSourceId(Long compteSourceId);
    boolean existsByCompteSourceIdAndCompteCibleId(Long sourceId, Long cibleId);
    @Transactional
    void deleteByCompteSourceIdAndCompteCibleId(Long idSource, Long idCible);
}
