package com.etudlife.repository;

import com.etudlife.model.Compte;
import com.etudlife.model.JobPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPreferenceRepository extends JpaRepository<JobPreference, Long> {

    JobPreference findByCompte(Compte compte);
}