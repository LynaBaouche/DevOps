package com.etudlife.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScopeGuardService {

    private final List<String> keywords = List.of(
            "absence","absences","justificatif","certificat",
            "examen","examens","rattrapage","preuve",
            "handicap","amenagement","tiers temps",
            "inscription","scolarite","secretariat"
    );

    public boolean isInScope(String question) {
        if (question == null) return false;
        String q = question.toLowerCase();
        return keywords.stream().anyMatch(q::contains);
    }
}
