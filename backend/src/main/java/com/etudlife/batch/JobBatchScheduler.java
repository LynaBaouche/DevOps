package com.etudlife.batch;

import com.etudlife.dto.JobDTO;
import com.etudlife.model.*;
import com.etudlife.repository.JobPreferenceRepository;
import com.etudlife.repository.SavedJobRepository;
import com.etudlife.service.JobSearchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Component
public class JobBatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobBatchScheduler.class);

    private final JobPreferenceRepository preferenceRepo;
    private final JobSearchService jobSearchService;
    private final SavedJobRepository savedJobRepo;

    public JobBatchScheduler(JobPreferenceRepository preferenceRepo,
                             JobSearchService jobSearchService,
                             SavedJobRepository savedJobRepo) {
        this.preferenceRepo = preferenceRepo;
        this.jobSearchService = jobSearchService;
        this.savedJobRepo = savedJobRepo;
    }

    // 🕒 Se lance tous les jours à 2h00 du matin
    // Cron: Secondes Minutes Heures JourMois Mois JourSemaine
    @Scheduled(cron = "0 0 2 * * ?")
    // En cas de test immédiat, remplacer par : @Scheduled(fixedRate = 60000) (Toutes les minutes)
    public void runNightlyBatch() {
        log.info("🌙 Début du Batch de nuit...");

        List<JobPreference> allPrefs = preferenceRepo.findAll();

        for (JobPreference pref : allPrefs) {
            if ("DESACTIVE".equals(pref.getFrequence())) continue;

            log.info("Traitement pour le compte : " + pref.getCompte().getId());
            processUserSearch(pref);
        }

        log.info("✅ Batch terminé.");
    }

    private void processUserSearch(JobPreference pref) {
        // 1. Construction de la requête
        StringBuilder query = new StringBuilder("Stage ");
        if (pref.getMotsCles() != null) query.append(pref.getMotsCles()).append(" ");
        if (pref.getLocalisation() != null) query.append(pref.getLocalisation()).append(" ");
        query.append("France");

        // 2. Appel API via le service
        List<JobDTO> results = jobSearchService.searchJobs(query.toString());

        // 3. Filtrage et Sauvegarde
        int countNew = 0;
        for (JobDTO jobApi : results) {
            // On vérifie si ce job est déjà en base
            Optional<SavedJob> existing = savedJobRepo.findByCompteAndExternalJobId(pref.getCompte(), jobApi.getJobId());

            if (existing.isEmpty()) {
                SavedJob newSuggestion = new SavedJob();
                newSuggestion.setCompte(pref.getCompte());
                newSuggestion.setExternalJobId(jobApi.getJobId());
                newSuggestion.setTitle(jobApi.getTitle());
                newSuggestion.setCompany(jobApi.getCompany());
                newSuggestion.setLocation(jobApi.getCity());
                newSuggestion.setApplyLink(jobApi.getApplyLink());
                newSuggestion.setStatus(JobStatus.SUGGESTION);

                savedJobRepo.save(newSuggestion);
                countNew++;
            }
        }
        log.info("   -> {} nouvelles suggestions trouvées.", countNew);
    }
}