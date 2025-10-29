package com.etudlife.service;

import com.etudlife.model.Document;
import com.etudlife.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;

@Service
public class DocumentService {

    @Value("${etudlife.upload.dir:uploads}")
    private String uploadDir;

    private final DocumentRepository repo;

    public DocumentService(DocumentRepository repo) {
        this.repo = repo;
    }

    public Document enregistrer(MultipartFile fichier, Long uploaderId, Long groupId) throws IOException {
        if (fichier.isEmpty()) throw new IOException("Fichier vide !");

        // Créer le dossier si inexistant
        Path dossier = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dossier);

        // Nom unique du fichier
        String nomFichier = Instant.now().toEpochMilli() + "_" + fichier.getOriginalFilename();
        Path chemin = dossier.resolve(nomFichier);
        Files.copy(fichier.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);

        // Créer et sauvegarder le document
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
    public java.util.List<Document> getAll() {
        return repo.findAll();
    }


}
