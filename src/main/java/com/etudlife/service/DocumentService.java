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

        if (fichier.isEmpty()) {
            throw new RuntimeException("Fichier vide !");
        }

        // 1️⃣ Vérifier doublon pour cet utilisateur
        Optional<Document> existing = repo.findByNomAndUploaderId(fichier.getOriginalFilename(), uploaderId);
        if (existing.isPresent()) {
            throw new RuntimeException("Ce fichier existe déjà !");
        }

        // 2️⃣ Vérifier taille max (20 Mo)
        long MAX_SIZE = 20 * 1024 * 1024;
        if (fichier.getSize() > MAX_SIZE) {
            throw new RuntimeException("Fichier trop volumineux (max 20 Mo)");
        }

        // 3️⃣ Créer le dossier si non présent
        Path dossier = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dossier);

        // NOM UNIQUE du fichier enregistré physiquement
        String nomFichierUnique = Instant.now().toEpochMilli() + "_" + fichier.getOriginalFilename();
        Path chemin = dossier.resolve(nomFichierUnique);

        // 4️⃣ Sauvegarde physique
        Files.copy(fichier.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);

        // 5️⃣ Sauvegarde en base
        Document doc = new Document();
        doc.setNom(fichier.getOriginalFilename());
        doc.setType(fichier.getContentType());
        doc.setTaille(fichier.getSize());
        doc.setChemin(nomFichierUnique); // 🔥 important : juste le nom
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
