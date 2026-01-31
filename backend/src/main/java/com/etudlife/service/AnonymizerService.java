package com.etudlife.service;

import org.springframework.stereotype.Service;

@Service
public class AnonymizerService {

    public String anonymize(String question) {
        if (question == null) return null;

        // Emails
        question = question.replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[REDACTED_EMAIL]");

        // IDs / numéros longs
        question = question.replaceAll("\\b\\d{8,}\\b", "[REDACTED_ID]");

        // "je m'appelle X"
        question = question.replaceAll("(?i)je\\s+m[' ]appelle\\s+\\p{L}+", "je m'appelle [REDACTED_NAME]");

        return question;
    }
}
