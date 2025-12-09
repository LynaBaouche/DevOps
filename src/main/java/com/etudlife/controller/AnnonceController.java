package com.etudlife.controller;

import com.etudlife.model.Annonce;
import com.etudlife.service.AnnonceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin(origins = "*")
public class AnnonceController {

    private final AnnonceService service;

    public AnnonceController(AnnonceService service) {
        this.service = service;
    }

    // 🔵 Récupérer toutes les annonces
    @GetMapping
    public List<Annonce> getAll(@RequestParam(required = false) String categorie) {
        if (categorie == null || categorie.equals("toutes")) {
            return service.findAll();
        }
        return service.findByCategorie(categorie);
    }

    // 🔵 Récupérer UNE annonce (pour le modal)
    @GetMapping("/{id}")
    public Annonce getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Annonce create(@RequestBody Annonce annonce) {
        return service.save(annonce);
    }
    @GetMapping("/utilisateur/{userId}")
    public List<Annonce> getByUser(@PathVariable Long userId) {
        return service.findByUtilisateurId(userId);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @PutMapping("/{id}/vue")
    public Annonce incrementVue(@PathVariable Long id) {
        Annonce a = service.findById(id);
        if (a == null) return null;

        a.setVues(a.getVues() + 1);
        return service.save(a);
    }
    @PutMapping("/{id}")
    public Annonce update(@PathVariable Long id, @RequestBody Annonce annonceModifiee) {
        Annonce a = service.findById(id);
        if (a == null) return null;

        a.setTitre(annonceModifiee.getTitre());
        a.setDescription(annonceModifiee.getDescription());
        a.setPrix(annonceModifiee.getPrix());
        a.setVille(annonceModifiee.getVille());
        a.setCategorie(annonceModifiee.getCategorie());
        a.setImage(annonceModifiee.getImage());

        return service.save(a);
    }




}
