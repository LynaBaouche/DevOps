package com.etudlife.controller;

import com.etudlife.model.Reservation;
import com.etudlife.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/reservation") // Correspond à l'URL dans ton catalogue.js
@CrossOrigin
public class ReservationController {

    @Autowired
    private ReservationService service;

    @PostMapping
    public ResponseEntity<String> creerReservation(
            @RequestParam Long iduser,
            @RequestParam Long livreId,
            @RequestParam String dateRecuperation,
            @RequestParam boolean domicile) {

        try {
            // Conversion de la String en LocalDate
            LocalDate date = LocalDate.parse(dateRecuperation);

            // On appelle le service pour enregistrer en base
            service.reserverLivre(livreId, iduser, date, domicile);

            return ResponseEntity.ok("Réservation confirmée");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Erreur : " + e.getMessage());
        }
    }

    // Pour afficher "Mes réservations"
    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<List<Reservation>> getReservations(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getMesReservations(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        service.annulerReservation(id);
        return ResponseEntity.noContent().build();
    }
}