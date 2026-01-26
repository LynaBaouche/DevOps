package com.etudlife.model;

import jakarta.persistence.*;
@Entity
@Table(name = "catalogue_general")
public class CatalogueLivre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String auteur;
    private String categorie;
    private boolean disponible = true; // Ajouté pour la réservation
    private int annee;
    private int pages;
    private String isbn;

    // --- GETTERS ET SETTERS ---
    public boolean isDisponible() { return disponible; }
    // Dans CatalogueLivre.java
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public void setDisponible(boolean b) {this.disponible= disponible;}
}