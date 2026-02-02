package com.etudlife.controller;

import com.etudlife.dto.JobDTO;
import com.etudlife.service.JobSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobSearchService jobSearchService;
    public JobController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
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
}