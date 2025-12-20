package com.etudlife.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private String prix;
    private String categorie;
    private String image;
    private String ville;
    private String auteur;
    private String datePublication;
    @Column(name = "utilisateur_id")
    private Long utilisateurId;
    @Column(columnDefinition = "INT DEFAULT 0")
    private int vues;
    private String lien;


}

