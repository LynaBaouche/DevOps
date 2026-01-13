package com.etudlife.service;

import com.etudlife.model.Compte;
import com.etudlife.model.Groupe;
import com.etudlife.model.Post;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import com.etudlife.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CompteRepository compteRepository;
    private final GroupeRepository groupeRepository;

    public PostService(PostRepository postRepository, CompteRepository compteRepository, GroupeRepository groupeRepository) {
        this.postRepository = postRepository;
        this.compteRepository = compteRepository;
        this.groupeRepository = groupeRepository;
    }

    public Post creerPost(Long auteurId, Long groupeId, String contenu) {
        Compte auteur = compteRepository.findById(auteurId)
                .orElseThrow(() -> new EntityNotFoundException("Auteur introuvable"));
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new EntityNotFoundException("Groupe introuvable"));

        Post post = new Post();
        post.setAuteur(auteur);
        post.setGroupe(groupe);
        post.setContenu(contenu);
        return postRepository.save(post);
    }

    public List<Post> getPostsParGroupe(Long groupeId) {
        return postRepository.findByGroupeIdOrderByDateCreationDesc(groupeId);
    }
    public List<Post> getHistoriqueAuteur(Long auteurId) {
        return postRepository.findByAuteurIdOrderByDateCreationDesc(auteurId);
    }
}