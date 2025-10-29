package com.etudlife.controller;

import com.etudlife.model.Document;
import com.etudlife.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
        
    }
    @GetMapping
    public ResponseEntity<?> getAllDocuments() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long uploaderId,
            @RequestParam(required = false) Long groupId
    ) {
        try {
            Document doc = service.enregistrer(file, uploaderId, groupId);
            return ResponseEntity.ok(doc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}

