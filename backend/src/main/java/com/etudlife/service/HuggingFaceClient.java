package com.etudlife.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class HuggingFaceClient {

    private final RestTemplate restTemplate;

    @Value("${hf.api.token}")
    private String token;

    @Value("${hf.api.url}")
    private String baseUrl;

    @Value("${hf.model}")
    private String model;

    public HuggingFaceClient() {
        // ✅ Timeouts (évite que ça bloque)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);

        this.restTemplate = new RestTemplate(factory);

        // ✅ Force UTF-8 pour éviter sÃ»r / prÃ©ciser
        this.restTemplate.getMessageConverters().forEach(c -> {
            if (c instanceof StringHttpMessageConverter conv) {
                conv.setDefaultCharset(StandardCharsets.UTF_8);
            }
        });
    }

    public String generate(String prompt) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("HF_API_TOKEN manquant. Mets-le dans .env et docker-compose.");
        }
        if (baseUrl == null || baseUrl.isBlank() || model == null || model.isBlank()) {
            throw new IllegalStateException("Config HF manquante: hf.api.url / hf.model.");
        }

        String url = baseUrl + model;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = Map.of("inputs", prompt);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            // utile pour diagnostiquer 401/429/503...
            return "HF_ERROR_HTTP_" + e.getStatusCode().value() + ": " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "HF_ERROR: " + e.getMessage();
        }
    }
}
