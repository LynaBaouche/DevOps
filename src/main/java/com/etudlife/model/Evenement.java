package com.etudlife.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    // ✅ NOUVEAU CHAMP
    private String couleur;
    @ManyToOne
    @JoinColumn(name = "compte_id")
    @JsonIgnoreProperties({"groupes", "liens", "posts", "evenements"})
    private Compte utilisateur;

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }
    public Compte getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Compte utilisateur) { this.utilisateur = utilisateur; }
}
