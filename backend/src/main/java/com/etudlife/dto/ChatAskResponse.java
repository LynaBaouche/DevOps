package com.etudlife.dto;

public class ChatAskResponse {
    private boolean success;
    private String question;
    private String response;
    private String confidence; // high/medium/low
    private String category;   // handicap/examens/absences/autre
    private String source;     // quel document utilisé

    public ChatAskResponse() {}

    public ChatAskResponse(boolean success, String question, String response,
                           String confidence, String category, String source) {
        this.success = success;
        this.question = question;
        this.response = response;
        this.confidence = confidence;
        this.category = category;
        this.source = source;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
