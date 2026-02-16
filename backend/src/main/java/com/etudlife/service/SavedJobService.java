package com.etudlife.service;

import com.etudlife.dto.SavedJobRequestDTO;
import com.etudlife.model.SavedJob;
import com.etudlife.model.JobStatus;
import com.etudlife.model.Compte;
import com.etudlife.repository.SavedJobRepository;
import com.etudlife.repository.CompteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final CompteRepository compteRepository;

    public SavedJobService(SavedJobRepository savedJobRepository, CompteRepository compteRepository) {
        this.savedJobRepository = savedJobRepository;
        this.compteRepository = compteRepository;
    }

    @Transactional
    public void saveOrUpdate(SavedJobRequestDTO dto) {
        // NOTE: Pour le moment, on récupère le premier compte par défaut
        // En attendant que ton système d'authentification soit totalement lié
        Compte compte = compteRepository.findAll().get(0);

        // On cherche si le job existe déjà pour cet utilisateur
        SavedJob savedJob = savedJobRepository.findByCompteAndExternalJobId(compte, dto.getExternalJobId())
                .orElse(new SavedJob());

        // On met à jour les infos
        savedJob.setCompte(compte);
        savedJob.setExternalJobId(dto.getExternalJobId());
        savedJob.setTitle(dto.getTitle());
        savedJob.setCompany(dto.getCompany());
        savedJob.setLocation(dto.getLocation());
        savedJob.setApplyLink(dto.getApplyLink());
        savedJob.setStatus(dto.getStatus()); // Ex: INTERESSE, POSTULE

        String link = dto.getApplyLink();
        if (link != null && link.length() > 250) {
            link = link.substring(0, 250); // On coupe à 250 caractères max
        }
        savedJob.setApplyLink(link);

        savedJobRepository.save(savedJob);
    }

    public List<SavedJob> getJobsByStatus(JobStatus status) {
        Compte compte = compteRepository.findAll().get(0);
        if (status == null) {
            return savedJobRepository.findByCompte(compte);
        }
        return savedJobRepository.findByCompteAndStatus(compte, status);
    }
}