package com.example.stickergenerator.model;

import java.util.List;

public class StickerCombo {
    private String targetName;
    private List<Sticker> stickers;
    private List<StickerPrice> prices;
    private double totalPrice;
    
    public StickerCombo() {}
    
    public StickerCombo(String targetName, List<Sticker> stickers) {
        this.targetName = targetName;
        this.stickers = stickers;
    }
    
    // Getters and setters
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public List<Sticker> getStickers() { return stickers; }
    public void setStickers(List<Sticker> stickers) { this.stickers = stickers; }
    
    public List<StickerPrice> getPrices() { return prices; }
    public void setPrices(List<StickerPrice> prices) { this.prices = prices; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
