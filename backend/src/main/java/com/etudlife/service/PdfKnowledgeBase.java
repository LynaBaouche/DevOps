package com.etudlife.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
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

    // Utilisation d'une seule liste "chunks"
    private final List<Chunk> chunks = new ArrayList<>();

    // Définition du Record avec Majuscule
    public record Chunk(String source, String text) {}

    // MÉTHODE QUE LE CHATSERVICE APPELLE
    public List<Chunk> searchInFile(String query, String fileName) {
        return this.search(query).stream()
                .filter(c -> c.source().equalsIgnoreCase(fileName))
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void load() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] pdfs = resolver.getResources(folder + "/*.pdf");

            for (Resource pdf : pdfs) {
                String name = pdf.getFilename() == null ? "unknown.pdf" : pdf.getFilename();
                String fullText = extractText(pdf);
                List<String> splitted = splitIntoChunks(fullText, chunkSizeChars);

                for (String c : splitted) {
                    String cleaned = c.replaceAll("\\s+", " ").trim();
                    if (!cleaned.isBlank()) chunks.add(new Chunk(name, cleaned));
                }
            }
            System.out.println("✅ Loaded PDF chunks: " + chunks.size());
        } catch (Exception e) {
            System.err.println("❌ PDF load error: " + e.getMessage());
        }
    }

    public List<Chunk> search(String question) {
        List<String> qTokens = tokenize(question);
        return chunks.stream()
                .map(c -> new ScoredChunk(c, score(qTokens, tokenize(c.text()))))
                .filter(s -> s.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(maxChunks)
                .map(s -> s.chunk)
                .collect(Collectors.toList());
    }

    private static class ScoredChunk {
        Chunk chunk; int score;
        ScoredChunk(Chunk c, int s){ this.chunk = c; this.score = s; }
    }

    private int score(List<String> q, List<String> t) {
        Set<String> set = new HashSet<>(t);
        int s = 0;
        for (String w : q) if (set.contains(w)) s++;
        return s;
    }

    private List<String> tokenize(String s) {
        return Arrays.stream(s.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-zàâçéèêëîïôûùüÿñæœ0-9 ]", " ")
                        .split("\\s+"))
                .filter(w -> w.length() >= 3)
                .collect(Collectors.toList());
    }

    private String extractText(Resource pdf) throws Exception {
        try (InputStream in = pdf.getInputStream(); PDDocument doc = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    private List<String> splitIntoChunks(String text, int size) {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + size, text.length());
            out.add(text.substring(i, end));
            i = end;
        }
        return out;
    }
    public String retrieveContext(String question) {
        List<Chunk> hits = this.search(question);
        return hits.stream()
                .map(Chunk::text)
                .collect(Collectors.joining("\n---\n"));
    }
}