package com.etudlife.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class EntreeMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jour;   // "Lundi", "Mardi"...
    private String moment; // "Midi" ou "Soir"

    @ManyToOne
    @JoinColumn(name = "recette_id")
    @JsonIgnoreProperties({"auteur"})
    private Recette recette;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnoreProperties({"menu", "recettes", "motDePasse", "groupes"})
    private Compte utilisateur;

    public EntreeMenu() {}

    public EntreeMenu(String jour, String moment, Recette recette, Compte utilisateur) {
        this.jour = jour;
        this.moment = moment;
        this.recette = recette;
        this.utilisateur = utilisateur;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }
    public String getMoment() { return moment; }
    public void setMoment(String moment) { this.moment = moment; }
    public Recette getRecette() { return recette; }
    public void setRecette(Recette recette) { this.recette = recette; }
    public Compte getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Compte utilisateur) { this.utilisateur = utilisateur; }
}