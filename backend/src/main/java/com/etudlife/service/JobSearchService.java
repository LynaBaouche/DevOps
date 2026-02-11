package com.etudlife.service;

import com.etudlife.dto.JobDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder; // IMPORTANT
import java.nio.charset.StandardCharsets; // IMPORTANT
import java.util.Collections;
import java.util.List;

@Service
public class JobSearchService {
    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);
    private final RestTemplate restTemplate;

    @Value("${RAPIDAPI_KEY:dummy}")
    private String apiKey;

    public JobSearchService() {
        this.restTemplate = new RestTemplate();
    }

    public List<JobDTO> searchJobs(String query) {
        try {

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = "https://jsearch.p.rapidapi.com/search"
                    + "?query=" + encodedQuery
                    + "&country=fr"
                    + "&language=fr";


            log.info("Appel JSearch pour : '{}' (URL: {})", query, url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", "jsearch.p.rapidapi.com");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JobResponseWrapper> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JobResponseWrapper.class
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                List<JobDTO> jobs = response.getBody().getData();
                log.info("✅ Succès : {} offres trouvées.", jobs.size());
                return jobs;
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("❌ Erreur API : ", e);
            return Collections.emptyList();
        }
    }

    private static class JobResponseWrapper {
        private List<JobDTO> data;
        public List<JobDTO> getData() { return data; }
        public void setData(List<JobDTO> data) { this.data = data; }
    }
}