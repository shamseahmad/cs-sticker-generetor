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
            
            // Create a mutable list instead of immutable
            List<StickerPrice> prices = priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());  // This creates a mutable ArrayList
            
            // Sort prices based on request
            if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                prices.sort(Comparator.comparingDouble(StickerPrice::getPrice).reversed());
            } else {
                prices.sort(Comparator.comparingDouble(StickerPrice::getPrice));
            }
            
            combo.setPrices(prices);
            combo.setTotalPrice(prices.stream().mapToDouble(StickerPrice::getPrice).sum());
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
}
