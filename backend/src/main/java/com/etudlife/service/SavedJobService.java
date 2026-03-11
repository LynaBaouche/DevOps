package com.etudlife.service;

import com.etudlife.dto.SavedJobRequestDTO;
import com.etudlife.model.SavedJob;
import com.etudlife.model.JobStatus;
import com.etudlife.model.Compte;
import com.etudlife.repository.SavedJobRepository;
import com.etudlife.repository.CompteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

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
        Compte compte = compteRepository.findById(dto.getCompteId())
                .orElseThrow(() -> new RuntimeException("Compte introuvable avec l'ID: " + dto.getCompteId()));

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

    public List<SavedJob> getJobsByStatus(JobStatus status, Long compteId) {
        Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));
        List<SavedJob> jobs = (status == null)
                ? savedJobRepository.findByCompte(compte)
                : savedJobRepository.findByCompteAndStatus(compte, status);

        // Dédoublonnage par externalJobId
        return jobs.stream()
                .collect(Collectors.toMap(
                        SavedJob::getExternalJobId,
                        j -> j,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .toList();
    }
}