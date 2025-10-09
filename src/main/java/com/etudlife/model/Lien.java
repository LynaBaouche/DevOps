package com.etudlife.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Lien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compte_source_id")
    private Compte compteSource;

    @ManyToOne
    @JoinColumn(name = "compte_cible_id")
    private Compte compteCible;

    private LocalDateTime dateCreation;

    public Lien() {}

    public Lien(Compte compteSource, Compte compteCible) {
        this.compteSource = compteSource;
        this.compteCible = compteCible;
        this.dateCreation = LocalDateTime.now();
    }

    // --- Getters et Setters ---
    public Long getId() { return id; }

    public Compte getCompteSource() { return compteSource; }
    public void setCompteSource(Compte compteSource) { this.compteSource = compteSource; }

    public Compte getCompteCible() { return compteCible; }
    public void setCompteCible(Compte compteCible) { this.compteCible = compteCible; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
