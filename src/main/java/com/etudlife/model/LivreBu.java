package com.etudlife.model;

import jakarta.persistence.*;

@Entity
@Table(name = "livre_bu")
public class LivreBu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String auteur;
    private Integer annee;
    private String isbn;
    private Integer pages;

    @Column(name = "disponible", columnDefinition = "BIT")
    private boolean disponible;

    // --- GETTERS ET SETTERS (Tous à l'intérieur des accolades de la classe) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

} // Fin de la classe