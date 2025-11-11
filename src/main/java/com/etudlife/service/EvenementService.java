package com.etudlife.service;

import com.etudlife.model.Evenement;
import com.etudlife.repository.EvenementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    public List<Evenement> getByUserId(Long id) {
        return evenementRepository.findByUtilisateurId(id);
    }

    public Evenement add(Evenement e) {
        return evenementRepository.save(e);
    }

    public void delete(Long id) {
        evenementRepository.deleteById(id);
    }
}
