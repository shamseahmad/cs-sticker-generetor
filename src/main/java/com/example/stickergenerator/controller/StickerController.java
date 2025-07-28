package com.example.stickergenerator.controller;

import com.example.stickergenerator.model.*;
import com.example.stickergenerator.service.NameGeneratorService;
import com.example.stickergenerator.service.SteamMarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stickers")
@CrossOrigin(origins = "*")
public class StickerController {
    private final NameGeneratorService nameGeneratorService;
    private final SteamMarketService steamMarketService;
    
    public StickerController(NameGeneratorService nameGeneratorService, 
                           SteamMarketService steamMarketService) {
        this.nameGeneratorService = nameGeneratorService;
        this.steamMarketService = steamMarketService;
    }
    
    @PostMapping("/generate")
    public ResponseEntity<List<StickerCombo>> generateStickerCombinations(
            @Valid @RequestBody NameRequest request) {
        
        List<StickerCombo> combinations = nameGeneratorService
            .generateNameCombinations(request.getName());
        
        // Fetch prices for each combination
        for (StickerCombo combo : combinations) {
            List<CompletableFuture<StickerPrice>> priceFutures = combo.getStickers().stream()
                .map(sticker -> steamMarketService.getStickerPrice(sticker.getFullName()))
                .collect(Collectors.toList());
            
            // Keep prices in same order as stickers to maintain correlation
            List<StickerPrice> prices = priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            combo.setPrices(prices);
            combo.setTotalPrice(prices.stream().mapToDouble(StickerPrice::getPrice).sum());
        }
        
        // Sort combinations by total price if requested
        if ("desc".equalsIgnoreCase(request.getSortOrder())) {
            combinations.sort(Comparator.comparingDouble(StickerCombo::getTotalPrice).reversed());
        } else if ("asc".equalsIgnoreCase(request.getSortOrder())) {
            combinations.sort(Comparator.comparingDouble(StickerCombo::getTotalPrice));
        }
        
        return ResponseEntity.ok(combinations);
    }

    @GetMapping("/debug/{name}")
    public ResponseEntity<Object> debugStickers(@PathVariable String name) {
    List<StickerCombo> combinations = nameGeneratorService.generateNameCombinations(name);
    
    Map<String, Object> debug = new HashMap<>();
    debug.put("inputName", name);
    debug.put("combinationsCount", combinations.size());
    debug.put("combinations", combinations);
    
    return ResponseEntity.ok(debug);
}
    
    @GetMapping("/search")
    public ResponseEntity<List<StickerCombo>> searchStickers(
            @RequestParam String name,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        
        NameRequest request = new NameRequest(name, sortOrder);
        return generateStickerCombinations(request);
    }
    
    @GetMapping("/debug/all-stickers")
    public ResponseEntity<Map<String, Object>> getAllStickers() {
        try {
            Map<String, Object> response = new HashMap<>();
            List<String> stickerNames = nameGeneratorService.getAllStickerNames();
            response.put("totalStickers", stickerNames.size());
            response.put("stickers", stickerNames);
            
            // Check specifically for s1mple and zywoo
            boolean hasS1mple = stickerNames.stream().anyMatch(s -> s.toLowerCase().contains("s1mple"));
            boolean hasZywoo = stickerNames.stream().anyMatch(s -> s.toLowerCase().contains("zywoo"));
            
            response.put("hasS1mple", hasS1mple);
            response.put("hasZywoo", hasZywoo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/debug/test-overlap")
    public ResponseEntity<Map<String, Object>> testOverlap() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Test the exact s1mple + zywoo scenario
            String target = "s1mplzywoo";
            response.put("target", target);
            
            // Check if stickers exist
            List<String> stickerNames = nameGeneratorService.getAllStickerNames();
            boolean hasS1mple = stickerNames.stream().anyMatch(s -> s.toLowerCase().contains("s1mple"));
            boolean hasZywoo = stickerNames.stream().anyMatch(s -> s.toLowerCase().contains("zywoo"));
            
            response.put("hasS1mple", hasS1mple);
            response.put("hasZywoo", hasZywoo);
            
            if (hasS1mple && hasZywoo) {
                // Manual test of overlapping logic
                String step1 = "s1mple"; // After placing s1mple
                String step2 = step1.substring(0, 5) + "zywoo"; // Overlap zywoo at position 5
                
                response.put("step1", step1);
                response.put("step2", step2);
                response.put("matches", step2.equals(target));
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
