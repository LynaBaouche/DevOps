package com.etudlife.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "id_livre")
    private Long idLivre;

    @Column(name = "date_reservation")
    private LocalDate dateReservation;

    @Column(name = "date_recuperation")
    private LocalDate dateRecuperation;

    @Column(name = "emprunt_domicile")
    private boolean empruntDomicile;
}
