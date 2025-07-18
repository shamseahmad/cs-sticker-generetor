package com.example.stickergenerator.service;

import com.example.stickergenerator.model.StickerPrice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SteamMarketService {
    private static final String STEAM_MARKET_BASE_URL = "https://steamcommunity.com/market/listings/730/";
    
    public CompletableFuture<StickerPrice> getStickerPrice(String stickerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Properly encode the sticker name for URL
                String encodedName = URLEncoder.encode(stickerName, StandardCharsets.UTF_8);
                String url = STEAM_MARKET_BASE_URL + encodedName;
                
                System.out.println("Fetching price for: " + stickerName);
                System.out.println("URL: " + url);
                
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000)
                    .get();
                
                Element priceElement = doc.select(".market_listing_price_with_fee").first();
                double price = 0.0;
                String currency = "USD";
                
                if (priceElement != null) {
                    String priceText = priceElement.text();
                    price = extractPrice(priceText);
                    currency = extractCurrency(priceText);
                    System.out.println("Found price: " + price + " " + currency);
                } else {
                    System.out.println("No price found for: " + stickerName);
                }
                
                return new StickerPrice(stickerName, price, currency, url);
                
            } catch (IOException e) {
                System.err.println("Error fetching price for " + stickerName + ": " + e.getMessage());
                // Return a default price with properly constructed URL
                String fallbackUrl = STEAM_MARKET_BASE_URL + URLEncoder.encode(stickerName, StandardCharsets.UTF_8);
                return new StickerPrice(stickerName, 0.0, "USD", fallbackUrl);
            }
        });
    }
    
    private double extractPrice(String priceText) {
        // Remove currency symbols and extract number
        String cleanPrice = priceText.replaceAll("[^0-9.,]", "");
        Pattern pricePattern = Pattern.compile("([0-9]{1,3}(?:[,][0-9]{3})*(?:[.][0-9]{2})?)");
        Matcher matcher = pricePattern.matcher(cleanPrice);
        
        if (matcher.find()) {
            String priceStr = matcher.group(1).replace(",", "");
            try {
                return Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                System.err.println("Could not parse price: " + priceStr);
                return 0.0;
            }
        }
        return 0.0;
    }
    
    private String extractCurrency(String priceText) {
        if (priceText.contains("$")) return "USD";
        if (priceText.contains("€")) return "EUR";
        if (priceText.contains("£")) return "GBP";
        return "USD";
    }
}
