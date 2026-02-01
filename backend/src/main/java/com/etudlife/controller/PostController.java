package com.etudlife.controller;

import com.etudlife.model.Post;
import com.etudlife.service.PostService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // Récupérer les posts d'un groupe
    @GetMapping("/groupe/{groupeId}")
    public List<Post> getPostsParGroupe(@PathVariable Long groupeId) {
        return postService.getPostsParGroupe(groupeId);
    }

    // Créer un post dans un groupe
    @PostMapping
    public Post creerPost(@RequestBody Map<String, Object> payload) {
        Long auteurId = Long.parseLong(payload.get("auteurId").toString());
        Long groupeId = Long.parseLong(payload.get("groupeId").toString());
        String contenu = payload.get("contenu").toString();

        return postService.creerPost(auteurId, groupeId, contenu);
    }
    @GetMapping("/auteur/{auteurId}")
    public List<Post> getHistorique(@PathVariable Long auteurId) {
        return postService.getHistoriqueAuteur(auteurId);
    }
}