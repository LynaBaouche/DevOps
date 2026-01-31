package com.etudlife.controller;

import com.etudlife.service.JobSearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobSearchService jobSearchService;

    public JobController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @GetMapping("/search")
    public String getJobs(@RequestParam String query) {
        return jobSearchService.searchJobs(query);
    }
}