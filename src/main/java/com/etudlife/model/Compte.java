package com.etudlife.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @OneToMany(mappedBy = "compteSource", cascade = CascadeType.ALL)
    private List<Lien> liens = new ArrayList<>();

    @ManyToMany(mappedBy = "membres")
    private List<Groupe> groupes = new ArrayList<>();

    public Compte() {}

    public Compte(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    // --- Getters et Setters ---
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public List<Lien> getLiens() { return liens; }
    public void setLiens(List<Lien> liens) { this.liens = liens; }

    public List<Groupe> getGroupes() { return groupes; }
    public void setGroupes(List<Groupe> groupes) { this.groupes = groupes; }
}
