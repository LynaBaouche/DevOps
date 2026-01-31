package com.etudlife.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public  class DocumentIAService {
    private final PdfTextExtractorService pdf;
    private final Map<String, String> cache = new HashMap<>();

    public DocumentIAService(PdfTextExtractorService pdf) {
        this.pdf = pdf;
    }

    // Charge un PDF depuis src/main/resources/rules/
    public String getPdfText(String pdfFilename) {
        return cache.computeIfAbsent(pdfFilename, name -> pdf.extractFromResources("rules", name));
    }
}