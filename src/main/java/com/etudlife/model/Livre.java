package com.etudlife.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "livre_bu")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String auteur;

    @Column(name = "dispo")
    private boolean disponible;

    private String section;
    private int annee;

    private String pages;   // était int → maintenant correct

    private String isbn;

    public boolean getDisponible() {return this.disponible ;
    }
}
