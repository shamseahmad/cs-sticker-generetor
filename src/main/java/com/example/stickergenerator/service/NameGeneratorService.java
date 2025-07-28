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
        System.out.println("Generating combinations for: '" + targetName + "'");
        
        List<Sticker> allStickers = stickerService.getAllStickers();
        System.out.println("Available stickers: " + allStickers.size());
        for (Sticker sticker : allStickers) {
            System.out.println("  - " + sticker.getExtractedName() + " (from: " + sticker.getFullName() + ")");
        }
        
        List<StickerCombo> combinations = new ArrayList<>();
        generateOverlappingCombinations(targetName.toLowerCase(), "", new ArrayList<>(), combinations);
        
        System.out.println("Generated " + combinations.size() + " combinations");
        return combinations;
    }
    
        /**
     * Generates sticker combinations using overlapping logic - stickers can overwrite parts of previous stickers
     */
    private void generateOverlappingCombinations(String targetName, String currentResult, 
                                               List<Sticker> currentCombo, List<StickerCombo> results) {
        
        // Simple approach: try all 2-sticker combinations
        if (currentCombo.isEmpty()) {
            List<Sticker> allStickers = stickerService.getAllStickers();
            
            System.out.println("🎯 Searching for: '" + targetName + "'");
            
            // Try each sticker as the first sticker
            for (Sticker sticker1 : allStickers) {
                String name1 = sticker1.getExtractedName().toLowerCase();
                
                // Try each sticker as the second sticker
                for (Sticker sticker2 : allStickers) {
                    if (sticker1.equals(sticker2)) continue; // Skip same sticker
                    
                    String name2 = sticker2.getExtractedName().toLowerCase();
                    
                    // Method 1: Simple concatenation
                    String concat = name1 + name2;
                    if (concat.equals(targetName)) {
                        System.out.println("🎉 FOUND (concat): " + name1 + " + " + name2 + " = " + concat);
                        results.add(new StickerCombo(targetName, Arrays.asList(sticker1, sticker2)));
                    }
                    
                    // Method 2: Overlapping - try cutting first sticker at different positions
                    for (int cutPos = 1; cutPos < name1.length(); cutPos++) {
                        String overlap = name1.substring(0, cutPos) + name2;
                        
                        if (overlap.equals(targetName)) {
                            System.out.println("🎉 FOUND (overlap): " + name1 + "[0:" + cutPos + "] + " + name2 + " = " + overlap);
                            results.add(new StickerCombo(targetName, Arrays.asList(sticker1, sticker2)));
                        }
                    }
                    
                    // Method 3: Reverse overlapping - try cutting second sticker  
                    for (int cutPos = 1; cutPos < name2.length(); cutPos++) {
                        String overlap = name1 + name2.substring(cutPos);
                        
                        if (overlap.equals(targetName)) {
                            System.out.println("🎉 FOUND (reverse): " + name1 + " + " + name2 + "[" + cutPos + ":] = " + overlap);
                            results.add(new StickerCombo(targetName, Arrays.asList(sticker1, sticker2)));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Places a sticker at a specific position, overwriting existing characters
     */
    private String placeSticker(String currentResult, String stickerName, int position) {
        String result;
        
        if (position >= currentResult.length()) {
            // Placing at or beyond the end - just append
            result = currentResult + stickerName;
            System.out.println("    🔗 Append mode: '" + currentResult + "' + '" + stickerName + "' = '" + result + "'");
        } else {
            // Overlapping placement - overwrite existing characters
            String before = currentResult.substring(0, position);
            String after = "";
            
            int endPosition = position + stickerName.length();
            if (endPosition < currentResult.length()) {
                after = currentResult.substring(endPosition);
            }
            
            result = before + stickerName + after;
            System.out.println("    ⚡ Overlap mode: before='" + before + "' + sticker='" + stickerName + 
                "' + after='" + after + "' = '" + result + "'");
        }
        
        return result;
    }
    
    /**
     * Checks if the current result is making valid progress toward the target
     */
    private boolean isValidProgress(String currentResult, String targetName) {
        // Allow much more flexibility for debugging
        if (currentResult.length() > targetName.length() + 5) {
            System.out.println("❌ Rejected '" + currentResult + "' - too long");
            return false;
        }
        
        // For now, be very permissive to see what combinations are being generated
        boolean isValid = true;
        
        // Check if we're making some reasonable progress
        if (currentResult.length() <= targetName.length()) {
            // Check if current result could be part of target
            isValid = targetName.contains(currentResult) || targetName.startsWith(currentResult);
        } else {
            // If longer than target, check if it starts with target
            isValid = currentResult.startsWith(targetName);
        }
        
        System.out.println((isValid ? "✅" : "❌") + " Progress check: '" + currentResult + 
            "' for target '" + targetName + "' → " + isValid);
        
        return isValid;
    }
    
    public List<String> getAllStickerNames() {
        return stickerService.getAllStickers().stream()
            .map(Sticker::getExtractedName)
            .toList();
    }
    

}
