package com.etudlife.controller;

import com.etudlife.model.Reservation;
import com.etudlife.service.BibliothequeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/reservation")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ReservationController {

    private final BibliothequeService bibliothequeService;

    @PostMapping
    public Reservation reserver(
            @RequestParam Long userId,
            @RequestParam Long livreId,
            @RequestParam String dateRecuperation,
            @RequestParam boolean domicile
    ) {
        return bibliothequeService.reserver(
                userId,
                livreId,
                LocalDate.parse(dateRecuperation),
                domicile
        );
    }

    @GetMapping("/user/{userId}")
    public List<Reservation> getByUser(@PathVariable Long userId) {
        return bibliothequeService.getReservationsUser(userId);
    }
}
