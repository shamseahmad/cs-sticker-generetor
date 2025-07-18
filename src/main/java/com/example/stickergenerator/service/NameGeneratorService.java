package com.example.stickergenerator.service;

import com.example.stickergenerator.model.Sticker;
import com.example.stickergenerator.model.StickerCombo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NameGeneratorService {
    private final StickerService stickerService;
    
    public NameGeneratorService(StickerService stickerService) {
        this.stickerService = stickerService;
    }
    
    public List<StickerCombo> generateNameCombinations(String targetName) {
        List<StickerCombo> combinations = new ArrayList<>();
        generateCombinations(targetName.toLowerCase(), 0, new ArrayList<>(), combinations);
        return combinations;
    }
    
    private void generateCombinations(String targetName, int startIndex, 
                                    List<Sticker> currentCombo, List<StickerCombo> results) {
        if (startIndex >= targetName.length()) {
            if (!currentCombo.isEmpty() && currentCombo.size() <= 5) {
                results.add(new StickerCombo(targetName, new ArrayList<>(currentCombo)));
            }
            return;
        }
        
        if (currentCombo.size() >= 5) {
            return; // Max 5 stickers
        }
        
        // Try different substring lengths
        for (int endIndex = startIndex + 1; endIndex <= targetName.length(); endIndex++) {
            String substring = targetName.substring(startIndex, endIndex);
            List<Sticker> matchingStickers = findMatchingStickers(substring);
            
            for (Sticker sticker : matchingStickers) {
                currentCombo.add(sticker);
                generateCombinations(targetName, endIndex, currentCombo, results);
                currentCombo.remove(currentCombo.size() - 1);
            }
        }
    }
    
    private List<Sticker> findMatchingStickers(String substring) {
        return stickerService.getAllStickers().stream()
            .filter(sticker -> sticker.getExtractedName().toLowerCase().startsWith(substring))
            .limit(10) // Limit to prevent too many combinations
            .toList();
    }
}
