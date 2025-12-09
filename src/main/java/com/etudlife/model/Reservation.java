package com.etudlife.model;

import java.time.LocalDate;

public class Reservation {

    private Long id;
    private Long bookId;
    private Long userId;
    private String type; // "domicile" ou "sur place"
    private int duree;   // 7 jours, 14 jours etc.
    private LocalDate dateRecuperation;
    private String commentaire;


    // --------- GETTERS / SETTERS ----------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
