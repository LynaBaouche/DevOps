package com.etudlife.controller;

import com.etudlife.dto.JobDTO;
import com.etudlife.dto.SavedJobRequestDTO;
import com.etudlife.model.JobStatus;
import com.etudlife.model.SavedJob;
import com.etudlife.service.JobSearchService;
import com.etudlife.service.SavedJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobSearchService jobSearchService;
    private final SavedJobService savedJobService;

    public JobController(JobSearchService jobSearchService, SavedJobService savedJobService) {
        this.jobSearchService = jobSearchService;
        this.savedJobService = savedJobService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobDTO>> search(
            @RequestParam(defaultValue = "Stage") String query,
            @RequestParam(required = false) String location) {

        StringBuilder fullQuery = new StringBuilder(query);

        // ✅ ON FORCE "FRANCE"
        // Si l'utilisateur tape "Stage", on envoie "Stage France"
        // Si l'utilisateur tape "Alternance" et location "Lyon", on envoie "Alternance Lyon France"

        if (location != null && !location.isBlank()) {
            fullQuery.append(" ").append(location);
        }

        // On ajoute toujours "France" pour éviter les résultats US (Théâtre)
        if (!fullQuery.toString().toLowerCase().contains("france")) {
            fullQuery.append(" France");
        }

        List<JobDTO> jobs = jobSearchService.searchJobs(fullQuery.toString());
        return ResponseEntity.ok(jobs);
    }

    // --- Les autres méthodes ne changent pas ---
    @PostMapping("/save")
    public ResponseEntity<?> saveJobStatus(@RequestBody SavedJobRequestDTO request) {
        savedJobService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<List<SavedJob>> getMyJobs(@RequestParam(required = false) JobStatus status) {
        return ResponseEntity.ok(savedJobService.getJobsByStatus(status));
    }
}