package com.etudlife.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;


    @OneToMany(mappedBy = "compteSource", cascade = CascadeType.MERGE, orphanRemoval = true)
    @JsonIgnoreProperties({"compteSource", "compteCible"})
    private List<Lien> liens = new ArrayList<>();


    @ManyToMany(mappedBy = "membres")
    @JsonIgnoreProperties({"membres"})
    private List<Groupe> groupes = new ArrayList<>();

    public Compte() {}

    public Compte(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public List<Lien> getLiens() { return liens; }
    public void setLiens(List<Lien> liens) { this.liens = liens; }

    public List<Groupe> getGroupes() { return groupes; }
    public void setGroupes(List<Groupe> groupes) { this.groupes = groupes; }
}
