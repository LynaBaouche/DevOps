package com.etudlife.model;


import jakarta.persistence.*;
import java.time.Instant;

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

    // ✅ On ajoute l'annotation @Lob pour stocker le fichier
    // ✅ On précise LONGBLOB pour MySQL (pour supporter les fichiers lourds)
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] donnees;

    // Optionnel : tu peux garder cette ligne si tu veux garder une trace
    // ou si tu ne veux pas modifier ta table SQL tout de suite
    private String chemin;

    private Long uploaderId;
    private Long groupId;

    // --- GETTERS ET SETTERS ---

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

    public byte[] getDonnees() { return donnees; }
    public void setDonnees(byte[] donnees) { this.donnees = donnees; }

    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public void setNomFichier(String nomFichier) {
        this.nom = nomFichier;
    }

    public String getNomFichier() {
        return nom;
    }
}