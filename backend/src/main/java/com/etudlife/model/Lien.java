package com.etudlife.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Lien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compte_source_id")
    @JsonIgnoreProperties({"liens", "groupes"})
    private Compte compteSource;

    @ManyToOne
    @JoinColumn(name = "compte_cible_id")
    @JsonIgnoreProperties({"liens", "groupes"})
    private Compte compteCible;

    private LocalDateTime dateCreation = LocalDateTime.now();

    public Lien() {}

    public Lien(Compte compteSource, Compte compteCible) {
        this.compteSource = compteSource;
        this.compteCible = compteCible;
        this.dateCreation = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Compte getCompteSource() { return compteSource; }
    public void setCompteSource(Compte compteSource) { this.compteSource = compteSource; }

    public Compte getCompteCible() { return compteCible; }
    public void setCompteCible(Compte compteCible) { this.compteCible = compteCible; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
