package com.etudlife.dto;

public class ChatAskRequest {
    private String question;

    public ChatAskRequest() {}

    public ChatAskRequest(String question) {
        this.question = question;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
