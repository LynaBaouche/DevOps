package com.etudlife.service;

import com.etudlife.model.Document;
import com.etudlife.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Value("${etudlife.upload.dir:uploads}")
    private String uploadDir;

    private final DocumentRepository repo;

    public DocumentService(DocumentRepository repo) {
        this.repo = repo;
    }

    // Enregistrer un fichier dans le dossier + base
    public Document enregistrer(MultipartFile fichier, Long uploaderId, Long groupId) throws IOException {
        if (fichier.isEmpty()) throw new RuntimeException("Fichier vide !");

        // On crée l'objet Document
        Document doc = new Document();
        doc.setNom(fichier.getOriginalFilename());
        doc.setType(fichier.getContentType());
        doc.setTaille(fichier.getSize());
        doc.setUploaderId(uploaderId);
        doc.setGroupId(groupId);
        doc.setDateUpload(Instant.now());

        // 🔥 On enregistre les octets du fichier directement
        doc.setDonnees(fichier.getBytes());

        return repo.save(doc);
    }

    // Récupérer tous les documents
    public List<Document> getAllDocuments() {
        return repo.findAll();
    }

    // Récupérer un document par ID
    public Optional<Document> getDocumentById(Long id) {
        return repo.findById(id);
    }

}