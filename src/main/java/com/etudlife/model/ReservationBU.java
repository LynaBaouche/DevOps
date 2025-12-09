package com.etudlife.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_bu")
public class ReservationBU {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idLivre;
    private Long idUser;
    private String type;
    private int duree;
    private LocalDate dateRecuperation;
    private String commentaire;

    private LocalDateTime dateReservation = LocalDateTime.now();

    public Long getIdLivre() { return idLivre ;}

}
