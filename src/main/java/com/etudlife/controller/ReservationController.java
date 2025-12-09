package com.etudlife.controller;

import com.etudlife.model.ReservationBU;
import com.etudlife.repository.ReservationBURepository;
import com.etudlife.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin("*")
public class ReservationController {

    @Autowired
    private ReservationService service;

    @PostMapping
    public ResponseEntity<?> reserver(@RequestBody ReservationBU reservation) {
        return ResponseEntity.ok(service.reserver(reservation));
    }
    @GetMapping("/user/{idUser}")
    public List<ReservationBU> getReservationsUser(@PathVariable Long idUser) {
        ReservationBURepository reservationRepo = null;
        return reservationRepo.findByIdUser(idUser);
    }

}
