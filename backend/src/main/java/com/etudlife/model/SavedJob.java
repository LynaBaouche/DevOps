package com.etudlife.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @Column(name = "date_sauvegarde")
    private LocalDateTime dateSauvegarde = LocalDateTime.now();
    // On copie les infos utiles de l'API pour ne pas avoir à la rappeler
    private String externalJobId; // L'ID unique fourni par JSearch
    private String title;
    private String company;
    private String location;
    private String applyLink;

    @Enumerated(EnumType.STRING)
    private JobStatus status; // LE champ important
    public LocalDateTime getDateSauvegarde() { return dateSauvegarde; }
    public void setDateSauvegarde(LocalDateTime dateSauvegarde) { this.dateSauvegarde = dateSauvegarde; }
}