package com.etudlife.controller;

import com.etudlife.model.Document;
import com.etudlife.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // ✅ Récupérer tous les documents
    @GetMapping
    public ResponseEntity<?> getAllDocuments() {
        return ResponseEntity.ok(service.getAllDocuments());
    }

    // ✅ Upload document
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam(value = "groupId", required = false) Long groupId
    ) {
        try {
            Document saved = service.enregistrer(file, uploaderId, groupId);
            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erreur lors de l'upload du fichier");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur inattendue : " + e.getMessage());
        }
    }

    // ✅ TÉLÉCHARGER UN DOCUMENT
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> telechargerDocument(@PathVariable Long id) {
        Document doc = service.getDocumentById(id)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNom() + "\"")
                .body(doc.getDonnees()); // On envoie les octets stockés en base
    }

}