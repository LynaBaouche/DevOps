package com.etudlife.repository;

import com.etudlife.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Récupère les posts d'un groupe, triés par date (les plus récents en premier)
    List<Post> findByGroupeIdOrderByDateCreationDesc(Long groupeId);
}