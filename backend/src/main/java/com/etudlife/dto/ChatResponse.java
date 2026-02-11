package com.etudlife.dto;

public class ChatResponse {
    private boolean success;
    private String sessionId;
    private String question;
    private String response;
    private String source;

    public ChatResponse(boolean success, String sessionId, String question, String response, String source) {
        this.success = success;
        this.sessionId = sessionId;
        this.question = question;
        this.response = response;
        this.source = source;
    }

    public boolean isSuccess() { return success; }
    public String getSessionId() { return sessionId; }
    public String getQuestion() { return question; }
    public String getResponse() { return response; }
    public String getSource() { return source; }
}
