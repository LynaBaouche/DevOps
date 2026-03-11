package com.etudlife.dto;

public class ChatRequest {
    private String sessionId;
    private String question;
    private String mode;
    private Long compteId; // <-- AJOUT ICI

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Long getCompteId() { return compteId; } // <-- AJOUT ICI
    public void setCompteId(Long compteId) { this.compteId = compteId; } // <-- AJOUT ICI
}
