package com.etudlife.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;

    @ManyToMany
    @JoinTable(
            name = "groupe_membres",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "compte_id")
    )
    @JsonIgnoreProperties({"groupes", "liens"}) // ✅ on coupe la récursion
    private List<Compte> membres = new ArrayList<>();

    public Groupe() {}

    public Groupe(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public void ajouterMembre(Compte compte) {
        if (!membres.contains(compte)) {
            membres.add(compte);
        }
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Compte> getMembres() { return membres; }
    public void setMembres(List<Compte> membres) { this.membres = membres; }
}
