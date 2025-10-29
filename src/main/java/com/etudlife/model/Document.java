package com.etudlife.model;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String type;
    private long taille;
    private Instant dateUpload = Instant.now();
    private String chemin;

    // optionnel : rattachement à un utilisateur ou groupe
    private Long uploaderId;
    private Long groupId;

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getTaille() { return taille; }
    public void setTaille(long taille) { this.taille = taille; }
    public Instant getDateUpload() { return dateUpload; }
    public void setDateUpload(Instant dateUpload) { this.dateUpload = dateUpload; }
    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }
    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
}
