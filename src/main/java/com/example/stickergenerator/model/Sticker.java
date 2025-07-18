package com.example.stickergenerator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sticker {
    private String fullName;
    private String extractedName;
    private String rarity;
    private String tournament;
    
    public Sticker() {}
    
    public Sticker(String fullName, String extractedName, String rarity, String tournament) {
        this.fullName = fullName;
        this.extractedName = extractedName;
        this.rarity = rarity;
        this.tournament = tournament;
    }
    
    // Getters and setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getExtractedName() { return extractedName; }
    public void setExtractedName(String extractedName) { this.extractedName = extractedName; }
    
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    
    public String getTournament() { return tournament; }
    public void setTournament(String tournament) { this.tournament = tournament; }
    
    @Override
    public String toString() {
        return "Sticker{" +
                "fullName='" + fullName + '\'' +
                ", extractedName='" + extractedName + '\'' +
                ", rarity='" + rarity + '\'' +
                ", tournament='" + tournament + '\'' +
                '}';
    }
}
