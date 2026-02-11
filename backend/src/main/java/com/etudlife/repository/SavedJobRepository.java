package com.etudlife.repository;

import com.etudlife.model.SavedJob;
import com.etudlife.model.JobStatus;
import com.etudlife.model.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByCompte(Compte compte);


    List<SavedJob> findByCompteAndStatus(Compte compte, JobStatus status);


    Optional<SavedJob> findByCompteAndExternalJobId(Compte compte, String externalJobId);
}