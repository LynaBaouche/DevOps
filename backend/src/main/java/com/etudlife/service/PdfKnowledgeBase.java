package com.etudlife.service;

import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PdfKnowledgeBase {

    @Value("${knowledge.folder:classpath:knowledge}")
    private String folder;

    @Value("${knowledge.chunkSizeChars:900}")
    private int chunkSizeChars;

    @Value("${knowledge.maxChunks:4}")
    private int maxChunks;

    private final List<Chunk> chunks = new ArrayList<>();

    // docType : "SITE" ou "REGLEMENT"
    public record Chunk(String source, String text, String docType) {}

    @PostConstruct
    public void load() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] pdfs = resolver.getResources(folder + "/*.pdf");

            for (Resource pdf : pdfs) {
                String name = (pdf.getFilename() == null) ? "unknown.pdf" : pdf.getFilename();
                String type = detectDocType(name); // ✅ calculé 1 seule fois par pdf

                String fullText = extractText(pdf);
                List<String> splitted = splitIntoChunks(fullText, chunkSizeChars);

                for (String c : splitted) {
                    String cleaned = c.replaceAll("\\s+", " ").trim();
                    if (!cleaned.isBlank()) {
                        chunks.add(new Chunk(name, cleaned, type));
                    }
                }
            }

            System.out.println("✅ Loaded PDF chunks: " + chunks.size());
        } catch (Exception e) {
            System.err.println("❌ PDF load error: " + e.getMessage());
        }
    }

    // ✅ garde la méthode simple (AUTO)
    public List<Chunk> search(String question) {
        return search(question, "AUTO");
    }

    // ✅ recherche filtrée par mode : AUTO | SITE | REGLEMENT
    public List<Chunk> search(String question, String mode) {
        String m = (mode == null) ? "AUTO" : mode.trim().toUpperCase(Locale.ROOT);
        if (!m.equals("SITE") && !m.equals("REGLEMENT") && !m.equals("AUTO")) {
            m = "AUTO";
        }

        // ✅ variable finale pour éviter: "Variable used in lambda..."
        final String mFinal = m;

        List<String> qTokens = tokenize(question);

        return chunks.stream()
                .filter(c -> mFinal.equals("AUTO") || c.docType().equalsIgnoreCase(mFinal))
                .map(c -> new ScoredChunk(c, score(qTokens, tokenize(c.text()))))
                .filter(s -> s.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(maxChunks)
                .map(s -> s.chunk)
                .collect(Collectors.toList());
    }

    private static class ScoredChunk {
        Chunk chunk;
        int score;

        ScoredChunk(Chunk c, int s) {
            this.chunk = c;
            this.score = s;
        }
    }

    // Score simple = nb de mots communs
    private int score(List<String> q, List<String> t) {
        Set<String> set = new HashSet<>(t);
        int s = 0;
        for (String w : q) {
            if (set.contains(w)) s++;
        }
        return s;
    }

    private List<String> tokenize(String s) {
        if (s == null) return List.of();

        return Arrays.stream(
                        s.toLowerCase(Locale.ROOT)
                                .replaceAll("[^a-zàâçéèêëîïôûùüÿñæœ0-9 ]", " ")
                                .split("\\s+")
                )
                .filter(w -> w.length() >= 3)
                .collect(Collectors.toList());
    }

    // ✅ FIX: le nettoyage est appliqué (tu ne retournes plus getText(doc) 2 fois)
    private String extractText(Resource pdf) throws Exception {
        try (InputStream in = pdf.getInputStream(); PDDocument doc = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);
            return text.replace("\uFFFD", "");
        }
    }

    private List<String> splitIntoChunks(String text, int size) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank()) return out;

        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + size, text.length());
            out.add(text.substring(i, end));
            i = end;
        }
        return out;
    }

    private String detectDocType(String filename) {
        if (filename == null) return "REGLEMENT";
        String f = filename.toLowerCase(Locale.ROOT);

        // ✅ Si le nom indique clairement du règlement → REGLEMENT
        if (f.contains("reglement") || f.contains("règlement")
                || f.contains("charte") || f.contains("examen") || f.contains("examens")
                || f.contains("fraude") || f.contains("plagiat") || f.contains("bizutage")) {
            return "REGLEMENT";
        }

        // ✅ Si le nom indique clairement un guide du site → SITE
        if (f.contains("site") || f.contains("fonction") || f.contains("fonctionnement")
                || f.contains("guide") || f.contains("manuel") || f.contains("tuto")) {
            return "SITE";
        }

        // ✅ Par défaut : REGLEMENT (plus safe pour ton cas)
        return "REGLEMENT";
    }

}
