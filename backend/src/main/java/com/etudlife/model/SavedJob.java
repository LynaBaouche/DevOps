package com.etudlife.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
public class SavedJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compte_id")
    private Compte compte; // Lien avec l'étudiant

    // On copie les infos utiles de l'API pour ne pas avoir à la rappeler
    private String externalJobId; // L'ID unique fourni par JSearch
    private String title;
    private String company;
    private String location;
    private String applyLink;

    @Enumerated(EnumType.STRING)
    private JobStatus status; // LE champ important
}