package com.etudlife.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    // ✅ nouveaux champs nécessaires pour login
    @Column(unique = true)
    private String email;

    private String motDePasse;
    private LocalDateTime lastConnection;

    @ManyToMany(mappedBy = "membres", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"membres", "posts"})
    private List<Groupe> groupes = new ArrayList<>();

    @OneToMany(mappedBy = "compteSource", cascade = CascadeType.MERGE, orphanRemoval = true)
    @JsonIgnoreProperties({"compteSource", "compteCible"})
    private List<Lien> liens = new ArrayList<>();

    @OneToMany(mappedBy = "auteur")
    @JsonIgnoreProperties({"auteur"})
    private List<Post> posts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "favoris_recettes",
            joinColumns = @JoinColumn(name = "compte_id"),
            inverseJoinColumns = @JoinColumn(name = "recette_id")
    )
    private Set<Recette> recettesFavorites = new HashSet<>();
    public Compte() {}

    public Compte(String prenom, String nom, String email, String motDePasse) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public List<Groupe> getGroupes() { return groupes; }
    public void setGroupes(List<Groupe> groupes) { this.groupes = groupes; }

    public List<Lien> getLiens() { return liens; }
    public void setLiens(List<Lien> liens) { this.liens = liens; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    public Set<Recette> getRecettesFavorites() {
        return recettesFavorites;
    }
    public void setRecettesFavorites(Set<Recette> recettesFavorites) {this.recettesFavorites = recettesFavorites;}

    public LocalDateTime getLastConnection() { return lastConnection;}
    public void setLastConnection(LocalDateTime lastConnection) {this.lastConnection = lastConnection;}
}
