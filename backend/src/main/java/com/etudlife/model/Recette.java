package com.etudlife.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Recette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description; // Étapes

    private Double prixEstime;
    private String tempsPreparation;
    private String categorie; // "fish", "meat", "vege", "dessert"
    private String image;
    @ElementCollection
    @CollectionTable(name = "recette_ingredients", joinColumns = @JoinColumn(name = "recette_id"))
    @Column(name = "ingredient")
    private List<String> ingredients;

    // Constructeurs
    public Recette() {}

    public Recette(String titre, String description, Double prixEstime, String tempsPreparation, String categorie, List<String> ingredients, String image) {
        this.titre = titre;
        this.description = description;
        this.prixEstime = prixEstime;
        this.tempsPreparation = tempsPreparation;
        this.categorie = categorie;
        this.ingredients = ingredients;
        this.image = image;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrixEstime() { return prixEstime; }
    public void setPrixEstime(Double prixEstime) { this.prixEstime = prixEstime; }
    public String getTempsPreparation() { return tempsPreparation; }
    public void setTempsPreparation(String tempsPreparation) { this.tempsPreparation = tempsPreparation; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}