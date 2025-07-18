package com.example.stickergenerator.model;

public class StickerPrice {
    private String stickerName;
    private double price;
    private String currency;
    private String marketUrl;
    
    public StickerPrice() {}
    
    public StickerPrice(String stickerName, double price, String currency, String marketUrl) {
        this.stickerName = stickerName;
        this.price = price;
        this.currency = currency;
        this.marketUrl = marketUrl;
    }
    
    // Getters and setters
    public String getStickerName() { return stickerName; }
    public void setStickerName(String stickerName) { this.stickerName = stickerName; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getMarketUrl() { return marketUrl; }
    public void setMarketUrl(String marketUrl) { this.marketUrl = marketUrl; }
}
