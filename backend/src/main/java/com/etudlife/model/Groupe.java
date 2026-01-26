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
    private String categorie;
    // ✅ Relation ManyToMany corrigée
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "groupe_membres",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "compte_id")
    )
    @JsonIgnoreProperties({"groupes", "liens", "posts"}) // 🔁 évite récursions
    private List<Compte> membres = new ArrayList<>();

    // ✅ Relation avec les posts
    @OneToMany(mappedBy = "groupe", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"groupe", "auteur"})
    private List<Post> posts = new ArrayList<>();

    // === Constructeurs ===
    public Groupe() {}

    public Groupe(String nom, String description, String categorie) {
        this.nom = nom;
        this.description = description;
        this.categorie= categorie;
    }

    // === Méthode utilitaire ===
    public void ajouterMembre(Compte compte) {
        if (!membres.contains(compte)) {
            membres.add(compte);
        }
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public List<Compte> getMembres() { return membres; }
    public void setMembres(List<Compte> membres) { this.membres = membres; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }
}
