package com.etudlife.service;

import com.etudlife.dto.JobDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;

@Service
public class JobSearchService {
    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);
    private final RestTemplate restTemplate;

    @Value("${RAPIDAPI_KEY:dummy}")
    private String apiKey;

    // Le constructeur initialise le RestTemplate
    public JobSearchService() {
        this.restTemplate = new RestTemplate();
    }

    public List<JobDTO> searchJobs(String query) {
        try {
            log.info("Appel JSearch via RapidAPI pour : {}", query);

            // 1. Configuration de l'URL (JSearch attend un paramètre 'query')
            String url = "https://jsearch.p.rapidapi.com/search?query=" + query;

            // 2. Configuration des Headers (Obligatoire pour RapidAPI)
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", "jsearch.p.rapidapi.com");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 3. Appel API
            ResponseEntity<JobResponseWrapper> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JobResponseWrapper.class
            );

            if (responseEntity.getBody() != null && responseEntity.getBody().getData() != null) {
                List<JobDTO> jobs = responseEntity.getBody().getData();
                log.info("Recherche réussie : {} offres trouvées", jobs.size());
                return jobs;
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Erreur critique lors de l'appel JSearch : {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Classe interne utilitaire pour mapper le format JSON de JSearch.
     * JSearch renvoie : { "status": "OK", "data": [...] }
     */
    private static class JobResponseWrapper {
        private List<JobDTO> data;

        public List<JobDTO> getData() { return data; }
        public void setData(List<JobDTO> data) { this.data = data; }
    }
}