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
        if (fichier.isEmpty()) throw new IOException("Fichier vide !");

        Path dossier = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dossier);

        String nomFichier = Instant.now().toEpochMilli() + "_" + fichier.getOriginalFilename();
        Path chemin = dossier.resolve(nomFichier);
        Files.copy(fichier.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);

        Document doc = new Document();
        doc.setNom(fichier.getOriginalFilename());
        doc.setType(fichier.getContentType());
        doc.setTaille(fichier.getSize());
        doc.setChemin(chemin.toString());
        doc.setUploaderId(uploaderId);
        doc.setGroupId(groupId);
        doc.setDateUpload(Instant.now());
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
