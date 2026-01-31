package com.etudlife.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class PdfTextExtractorService {

    public String extractFromResources(String folder, String pdfFilename) {
        try {
            ClassPathResource res = new ClassPathResource(folder + "/" + pdfFilename);

            try (InputStream is = res.getInputStream(); PDDocument doc = PDDocument.load(is)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(doc);
                return clean(text);
            }

        } catch (Exception e) {
            return "";
        }
    }

    private String clean(String text) {
        if (text == null) return "";
        text = text.replace("\u0000", "");
        text = text.replaceAll("[ \\t]+", " ");
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }
}
