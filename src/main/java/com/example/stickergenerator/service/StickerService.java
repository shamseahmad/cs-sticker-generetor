package com.example.stickergenerator.service;

import com.example.stickergenerator.model.Sticker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StickerService {
    private final ObjectMapper objectMapper;
    private List<Sticker> stickers;
    
    public StickerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadStickers();
    }
    
    private void loadStickers() {
        try {
            ClassPathResource resource = new ClassPathResource("data/stickers.json");
            if (!resource.exists()) {
                throw new RuntimeException("stickers.json file not found in src/main/resources/data/");
            }
            
            List<String> stickerNames = objectMapper.readValue(
                resource.getInputStream(), 
                new TypeReference<List<String>>() {}
            );
            
            System.out.println("Loaded " + stickerNames.size() + " stickers from JSON file");
            
            this.stickers = stickerNames.stream()
                .map(this::parseSticker)
                .toList();
                
            System.out.println("Parsed " + this.stickers.size() + " stickers");
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load stickers from JSON file: " + e.getMessage(), e);
        }
    }
    
    private Sticker parseSticker(String fullName) {
        // Pattern: "Sticker | Senzu (Gold) | Shanghai 2024"
        Pattern pattern = Pattern.compile("Sticker\\s*\\|\\s*([^|]+?)\\s*(?:\\([^)]*\\))?\\s*\\|\\s*(.+)");
        Matcher matcher = pattern.matcher(fullName);
        
        if (matcher.find()) {
            String extractedName = matcher.group(1).trim();
            String tournament = matcher.group(2).trim();
            String rarity = extractRarity(fullName);
            
            Sticker sticker = new Sticker(fullName, extractedName, rarity, tournament);
            System.out.println("Parsed sticker: " + extractedName + " from " + fullName);
            return sticker;
        }
        
        // If pattern doesn't match, use the full name as extracted name
        System.out.println("Pattern didn't match for: " + fullName + ", using as-is");
        return new Sticker(fullName, fullName, "", "");
    }
    
    private String extractRarity(String fullName) {
        Pattern rarityPattern = Pattern.compile("\\((Gold|Holo|Glitter)\\)");
        Matcher matcher = rarityPattern.matcher(fullName);
        return matcher.find() ? matcher.group(1) : "";
    }
    
    public List<Sticker> getAllStickers() {
        return stickers;
    }
    
    public List<Sticker> findStickersByLetter(char letter) {
        return stickers.stream()
            .filter(sticker -> sticker.getExtractedName().toLowerCase().startsWith(String.valueOf(letter).toLowerCase()))
            .toList();
    }
}
