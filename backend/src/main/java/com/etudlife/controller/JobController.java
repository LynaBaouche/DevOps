package com.etudlife.controller;

import com.etudlife.dto.JobDTO;
import com.etudlife.service.JobSearchService;
import com.etudlife.service.SavedJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.etudlife.dto.SavedJobRequestDTO;
import com.etudlife.model.JobStatus;
import com.etudlife.model.SavedJob;

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

        // On combine la recherche : "Stage Java" + "Paris"
        String fullQuery = query;
        if (location != null && !location.isEmpty()) {
            fullQuery += " à " + location;
        }

        List<JobDTO> jobs = jobSearchService.searchJobs(fullQuery);
        return ResponseEntity.ok(jobs);
    }
    @PostMapping("/save")
    public ResponseEntity<?> saveJobStatus(@RequestBody SavedJobRequestDTO request) {

        savedJobService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<List<SavedJob>> getMyJobs(@RequestParam(required = false) JobStatus status) {
        // Récupère les jobs filtrés par statut (ex: INTERESSE, POSTULE)
        List<SavedJob> myJobs = savedJobService.getJobsByStatus(status);
        return ResponseEntity.ok(myJobs);
    }
}