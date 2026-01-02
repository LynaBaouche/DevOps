package com.etudlife.controller;

import com.etudlife.model.Reservation;
import com.etudlife.service.BibliothequeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/livre_bu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BibliothequeController {

    private final BibliothequeService service;

    @PostMapping("/reserver")
    public Reservation reserver(
            @RequestParam Long userId,
            @RequestParam Long livreId,
            @RequestParam String dateRecuperation,
            @RequestParam boolean domicile
    ) {
        return service.reserver(
                userId,
                livreId,
                LocalDate.parse(dateRecuperation),
                domicile
        );
    }
}
