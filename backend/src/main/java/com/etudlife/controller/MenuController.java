package com.etudlife.controller;

import com.etudlife.model.EntreeMenu;
import com.etudlife.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService service;

    public MenuController(MenuService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public List<EntreeMenu> getMenu(@PathVariable Long userId) {
        return service.getMenuUtilisateur(userId);
    }

    @PostMapping("/generer")
    public ResponseEntity<?> generer(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            Double budget = Double.parseDouble(payload.get("budget").toString());

            List<EntreeMenu> menu = service.genererMenuAutomatique(userId, budget);
            return ResponseEntity.ok(menu);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}