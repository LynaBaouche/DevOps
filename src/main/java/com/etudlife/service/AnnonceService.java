package com.etudlife.service;



import com.etudlife.model.Annonce;
import com.etudlife.repository.AnnonceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnonceService {

    private final AnnonceRepository repository;

    public AnnonceService(AnnonceRepository repository) {
        this.repository = repository;
    }

    public List<Annonce> findAll() {
        return repository.findAll();
    }

    public List<Annonce> findByCategorie(String categorie) {
        return repository.findByCategorie(categorie);
    }
}
