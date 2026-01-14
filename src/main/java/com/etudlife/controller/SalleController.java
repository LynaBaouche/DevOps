package com.etudlife.controller;

import com.etudlife.model.ReservationSalle;
import com.etudlife.repository.ReservationSalleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salles")
@CrossOrigin
public class SalleController {
    @Autowired
    private ReservationSalleRepository repository;

    @PostMapping("/reserver")
    public ResponseEntity<String> reserver(@RequestBody ReservationSalle res) {
        repository.save(res);
        return ResponseEntity.ok("Place réservée avec succès !");
    }

    @GetMapping("/utilisateur/{userId}")
    public List<ReservationSalle> getMesSalles(@PathVariable Long userId) {
        return repository.findByIdUser(userId);
    }
}
