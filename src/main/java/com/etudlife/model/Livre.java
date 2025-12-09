package com.etudlife.model;

import jakarta.persistence.*;

@Entity
@Table(name = "livre_bu")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String auteur;
    private String section;
    private String annee;
    private String pages;
    private String isbn;
    private Boolean dispo;

    public boolean getDispo() {return  dispo;}

    public void setDispo(boolean dispo) {this.dispo = dispo;
    }
}
