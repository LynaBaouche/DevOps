package com.etudlife.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class JobPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pour l'instant on lie au compte par défaut, plus tard au user connecté
    @OneToOne
    private Compte compte;

    private String motsCles;
    private String localisation;
    private Integer remunerationMin;

    // Fréquence : QUOTIDIEN, HEBDOMADAIRE, DESACTIVE
    private String frequence;
}