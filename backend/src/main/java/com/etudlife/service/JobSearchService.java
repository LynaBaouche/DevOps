package com.etudlife.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class JobSearchService {

    private final String API_URL = "https://jsearch.p.rapidapi.com/search";

    @Value("${RAPIDAPI_KEY}")
    private String apiKey;

    public String searchJobs(String query) {
        RestTemplate restTemplate = new RestTemplate();

        // Préparation des headers avec ta clé API récupérée de Docker
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", apiKey); // <-- Ta variable est injectée ici
        headers.set("X-RapidAPI-Host", "jsearch.p.rapidapi.com");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Utilisation de UriComponentsBuilder pour construire une URL propre (gère les espaces et caractères spéciaux)
        // On ajoute "stage" et "France" à la requête pour être précis d'entrée de jeu
        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("query", query)
                .queryParam("num_pages", "1")
                .queryParam("country", "fr")  // <--- Ajoute ça pour la France
                .queryParam("language", "fr") // <--- Ajoute ça pour le français
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            // Log de l'erreur dans la console Docker pour debugger
            System.err.println("Erreur JSearch: " + e.getMessage());
            return "{\"error\": \"Impossible de récupérer les offres. Vérifiez votre clé API ou votre quota.\"}";
        }
    }
}