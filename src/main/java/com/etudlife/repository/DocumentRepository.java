package com.etudlife.repository;

import com.etudlife.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByNomAndUploaderId(String nom, Long uploaderId);

}
