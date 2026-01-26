package com.etudlife.model;

import java.sql.Date;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_livre")
    private Long idLivre;

    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "date_reservation")
    private LocalDate dateReservation;

    @Column(name = "date_recuperation")
    private LocalDate dateRecuperation;

    @Column(name = "emprunt_domicile")
    private Boolean empruntDomicile;

    // ===== GETTERS =====
    public Long getId() { return id; }
    public Long getIdLivre() { return idLivre; }
    public Long getIdUser() { return idUser; }
    public LocalDate getDateReservation() { return dateReservation; }
    public LocalDate getDateRecuperation() { return dateRecuperation; }
    public Boolean getEmpruntDomicile() { return empruntDomicile; }

    // ===== SETTERS (CE QUI MANQUAIT) =====
    public void setIdLivre(Long idLivre) { this.idLivre = idLivre; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }
    public void setDateReservation(LocalDate dateReservation) { this.dateReservation = dateReservation; }
    public void setDateRecuperation(LocalDate dateRecuperation) { this.dateRecuperation = dateRecuperation; }
    public void setEmpruntDomicile(Boolean empruntDomicile) { this.empruntDomicile = empruntDomicile; }
}
