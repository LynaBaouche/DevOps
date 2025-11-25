package com.etudlife.controller;

import com.etudlife.model.Document;
import com.etudlife.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin
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

    // ✅ Récupérer un document par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable Long id) {
        return service.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value="uploaderId", required=false) Long uploaderId,
            @RequestParam(value="groupId", required=false) Long groupId
    ) {
        try {
            Document saved = service.enregistrer(file, uploaderId, groupId);
            return ResponseEntity.ok(saved);

        } catch (IOException e) {   // ⚠️ spécifique → en premier
            return ResponseEntity.status(500).body("Erreur lors de l'upload du fichier");

        } catch (RuntimeException e) {  // exceptions métier
            return ResponseEntity.badRequest().body("Erreur serveur : " + e.getMessage());

        } catch (Exception e) {  // général → en dernier
            return ResponseEntity.status(500).body("Erreur inattendue : " + e.getMessage());
        }


    }


}
