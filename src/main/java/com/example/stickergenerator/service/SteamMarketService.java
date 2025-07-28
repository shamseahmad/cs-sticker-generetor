package com.example.stickergenerator.service;

import com.example.stickergenerator.model.StickerPrice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steam Market Service that fetches real market prices through web scraping.
 * Implements caching and proper anti-bot handling for reliable price fetching.
 */
@Service
public class SteamMarketService {
    private static final String STEAM_MARKET_SEARCH_URL = "https://steamcommunity.com/market/search?appid=730&q=";
    private static final String STEAM_MARKET_LISTINGS_URL = "https://steamcommunity.com/market/listings/730/";
    
    // Cache to store fetched prices and reduce Steam Market requests
    private final Map<String, Double> priceCache = new HashMap<>();
    
    // Request delay to avoid overwhelming Steam Market (reduced for more aggressive fetching)
    private static final long REQUEST_DELAY_MS = 1000; // Reduced from 2000ms to 1000ms
    private long lastRequestTime = 0;
    
    @PostConstruct
    public void init() {
        System.out.println("🚀 SteamMarketService initialized. Clearing price cache on startup.");
        clearPriceCache();
    }

    public CompletableFuture<StickerPrice> getStickerPrice(String stickerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                double price = fetchRealMarketPrice(stickerName);
                String searchUrl = STEAM_MARKET_SEARCH_URL + URLEncoder.encode(stickerName, StandardCharsets.UTF_8);
                return new StickerPrice(stickerName, price, "USD", searchUrl);
            } catch (Exception e) {
                System.err.println("❌ Failed to fetch price for " + stickerName + ": " + e.getMessage());
                // Fallback to reasonable default
                String searchUrl = STEAM_MARKET_SEARCH_URL + URLEncoder.encode(stickerName, StandardCharsets.UTF_8);
                return new StickerPrice(stickerName, 0.10, "USD", searchUrl);
            }
        });
    }
    
    /**
     * Fetches real market price from Steam Market through web scraping
     */
    private double fetchRealMarketPrice(String stickerName) {
        // Check cache first
        if (priceCache.containsKey(stickerName)) {
            double cachedPrice = priceCache.get(stickerName);
            System.out.println("💾 Using cached price for " + stickerName + ": $" + cachedPrice);
            return cachedPrice;
        }
        
        System.out.println("🌐 Fetching real market price for: " + stickerName);
        
        // FIRST: Try a simple test to see if we can reach Steam at all
        try {
            System.out.println("🔬 Testing Steam Market connectivity...");
            String testUrl = "https://steamcommunity.com/market/";
            Document testDoc = Jsoup.connect(testUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();
            System.out.println("✅ Steam Market is reachable. Title: " + testDoc.title());
        } catch (Exception e) {
            System.err.println("❌ Cannot reach Steam Market at all: " + e.getMessage());
            return getMinimalFallback();
        }
        
        try {
            // Multiple scraping attempts with different strategies
            double price = 0.0;
            
            // Strategy 1: Direct listings page with enhanced headers
            try {
                enforceRateLimit();
                price = scrapeListingsPageEnhanced(stickerName);
                if (price > 0) {
                    priceCache.put(stickerName, price);
                    System.out.println("💰 Real market price (enhanced listings) for " + stickerName + ": $" + price);
                    return price;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Enhanced listings page failed for " + stickerName + ": " + e.getMessage());
            }
            
            // Strategy 2: Try with very simple URL encoding
            try {
                enforceRateLimit();
                String simpleName = stickerName.replace("Sticker | ", "").replace(" | ", " ");
                price = scrapeListingsPageSimple(simpleName);
                if (price > 0) {
                    priceCache.put(stickerName, price);
                    System.out.println("💰 Real market price (simple) for " + stickerName + ": $" + price);
                    return price;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Simple scraping failed for " + stickerName + ": " + e.getMessage());
            }
            
            // Strategy 3: Use different user agent
            try {
                enforceRateLimit();
                price = scrapeWithDifferentAgent(stickerName);
                if (price > 0) {
                    priceCache.put(stickerName, price);
                    System.out.println("💰 Real market price (different agent) for " + stickerName + ": $" + price);
                    return price;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Different agent failed for " + stickerName + ": " + e.getMessage());
            }
            
            System.out.println("❌ All scraping strategies failed for " + stickerName + ", using minimal fallback");
            return getMinimalFallback();
            
        } catch (Exception e) {
            System.err.println("❌ Error scraping Steam Market for " + stickerName + ": " + e.getMessage());
            return getMinimalFallback();
        }
    }
    
    /**
     * Enhanced listings page scraper with better anti-bot protection bypass
     */
    private double scrapeListingsPageEnhanced(String stickerName) throws IOException {
        String encodedName = URLEncoder.encode(stickerName, StandardCharsets.UTF_8);
        String url = STEAM_MARKET_LISTINGS_URL + encodedName;
        
        System.out.println("🔍 Enhanced scraping listings page: " + url);
        
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("DNT", "1")
                // REMOVED: .header("Connection", "keep-alive") - This is restricted in Java
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1")
                .header("Cache-Control", "max-age=0")
                .timeout(8000)
                .followRedirects(true)
                .get();
        
        System.out.println("📄 Page title: " + doc.title());
        System.out.println("📄 Page text length: " + doc.text().length());
        
        // DEBUG: Save HTML content for inspection if no prices found
        if (doc.text().length() < 500) {
            System.out.println("⚠️ Very short page content, likely an error page:");
            System.out.println("📝 Full HTML: " + doc.outerHtml());
        }
        
        // DETAILED HTML INSPECTION - Let's see what Steam is actually returning
        System.out.println("🔍 HTML INSPECTION:");
        System.out.println("🔍 Body classes: " + doc.body().className());
        
        // Look for any element containing price-like text
        Elements allElements = doc.select("*");
        for (Element element : allElements) {
            String text = element.ownText().trim();
            if (text.contains("$") || text.contains("€") || text.contains("£")) {
                System.out.println("💰 Found $ element: " + element.tagName() + " class='" + element.className() + "' text='" + text + "'");
            }
        }
        
        // Check for common Steam Market selectors
        String[] selectors = {
            ".market_listing_price_with_fee",
            ".market_listing_price",
            ".market_table_value",
            ".normal_price",
            ".sale_price",
            "[id*=price]",
            "[class*=price]",
            "span:contains($)",
            "div:contains($)"
        };
        
        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                System.out.println("🎯 Found elements with selector '" + selector + "': " + elements.size());
                for (Element element : elements) {
                    System.out.println("   - " + element.tagName() + " class='" + element.className() + "' text='" + element.text() + "'");
                }
            }
        }
        
        // Look for the lowest price (first sell order)
        Elements priceElements = doc.select(".market_listing_price_with_fee");
        if (!priceElements.isEmpty()) {
            String priceText = priceElements.first().text().trim();
            System.out.println("💰 Found price element: " + priceText);
            return extractPrice(priceText);
        }
        
        // Alternative selector for price
        Elements altPriceElements = doc.select(".market_listing_price");
        if (!altPriceElements.isEmpty()) {
            String priceText = altPriceElements.first().text().trim();
            System.out.println("💰 Found alt price element: " + priceText);
            return extractPrice(priceText);
        }
        
        // Try broader selectors if specific ones fail
        Elements broadElements = doc.select("*:contains($)");
        if (!broadElements.isEmpty()) {
            System.out.println("💰 Found broad $ elements: " + broadElements.size());
            for (Element element : broadElements) {
                String text = element.ownText().trim();
                if (text.contains("$") && text.length() < 20) { // Likely a price
                    System.out.println("💰 Potential price: " + text);
                    double price = extractPrice(text);
                    if (price > 0) {
                        return price;
                    }
                }
            }
        }
        
        System.out.println("❌ No price elements found on page");
        return 0.0;
    }
    
    /**
     * Simple listings page scraper with minimal name processing
     */
    private double scrapeListingsPageSimple(String simpleName) throws IOException {
        String encodedName = URLEncoder.encode(simpleName, StandardCharsets.UTF_8);
        String url = STEAM_MARKET_LISTINGS_URL + encodedName;
        
        System.out.println("🔍 Simple scraping listings page: " + url);
        
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .timeout(6000)
                .get();
        
        System.out.println("📄 Simple page title: " + doc.title());
        
        // Look for price elements
        Elements priceElements = doc.select(".market_listing_price_with_fee, .market_listing_price");
        if (!priceElements.isEmpty()) {
            String priceText = priceElements.first().text().trim();
            System.out.println("💰 Found simple price: " + priceText);
            return extractPrice(priceText);
        }
        
        System.out.println("❌ No price found in simple scraping");
        return 0.0;
    }
    
    /**
     * Scraper with different user agent to avoid detection
     */
    private double scrapeWithDifferentAgent(String stickerName) throws IOException {
        String encodedName = URLEncoder.encode(stickerName, StandardCharsets.UTF_8);
        String url = STEAM_MARKET_LISTINGS_URL + encodedName;
        
        System.out.println("🔍 Different agent scraping: " + url);
        
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-us")
                .timeout(5000)
                .get();
        
        System.out.println("📄 Different agent page title: " + doc.title());
        
        // Look for price elements
        Elements priceElements = doc.select(".market_listing_price_with_fee, .market_listing_price");
        if (!priceElements.isEmpty()) {
            String priceText = priceElements.first().text().trim();
            System.out.println("💰 Found different agent price: " + priceText);
            return extractPrice(priceText);
        }
        
        System.out.println("❌ No price found with different agent");
        return 0.0;
    }
    
    /**
     * Scrapes the Steam Market search page for item price
     */
    private double scrapeSearchPage(String stickerName) throws IOException {
        String encodedName = URLEncoder.encode(stickerName, StandardCharsets.UTF_8);
        String url = STEAM_MARKET_SEARCH_URL + encodedName;
        
        System.out.println("🔍 Scraping search page: " + url);
        
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .timeout(10000)
                .get();
        
        // Look for search results with prices
        Elements searchResults = doc.select(".market_listing_row");
        for (Element result : searchResults) {
            String itemName = result.select(".market_listing_item_name").text();
            
            // Check if this result matches our sticker
            if (itemName.toLowerCase().contains(stickerName.toLowerCase()) || 
                stickerName.toLowerCase().contains(itemName.toLowerCase())) {
                
                Elements priceElements = result.select(".market_listing_price");
                if (!priceElements.isEmpty()) {
                    String priceText = priceElements.first().text().trim();
                    double price = extractPrice(priceText);
                    if (price > 0) {
                        System.out.println("✅ Found matching item: " + itemName + " - $" + price);
                        return price;
                    }
                }
            }
        }
        
        return 0.0;
    }
    
    /**
     * Enforces rate limiting to avoid overwhelming Steam Market (reduced for aggressive fetching)
     */
    private void enforceRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        
        if (timeSinceLastRequest < REQUEST_DELAY_MS) {
            try {
                long sleepTime = REQUEST_DELAY_MS - timeSinceLastRequest;
                System.out.println("⏱️ Rate limiting: waiting " + sleepTime + "ms (aggressive mode)");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
    
    /**
     * Provides fallback pricing when web scraping fails
     */
    private double getMinimalFallback() {
        return 0.05; // Default fallback
    }
    
    /**
     * Extracts price from Steam Market price text
     */
    private double extractPrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return 0.0;
        }
        
        System.out.println("🏷️ Extracting price from: '" + priceText + "'");
        
        // Handle different currency formats
        // USD: $1.23, EUR: €1,23, GBP: £1.23, etc.
        
        // Remove common prefixes/suffixes
        String cleanText = priceText.replace("Starting at:", "")
                                   .replace("Buy now for", "")
                                   .replace("Lowest price:", "")
                                   .trim();
        
        // Pattern to match various price formats
        Pattern pricePattern = Pattern.compile("([€$£¥₹])\\s*([0-9]{1,3}(?:[,\\s][0-9]{3})*(?:[.,][0-9]{1,2})?)");
        Matcher matcher = pricePattern.matcher(cleanText);
        
        if (matcher.find()) {
            String currency = matcher.group(1);
            String priceStr = matcher.group(2).replace(",", "").replace(" ", "");
            
            try {
                double price = Double.parseDouble(priceStr);
                
                // Convert to USD if needed (rough conversion)
                if (currency.equals("€")) price *= 1.1;
                else if (currency.equals("£")) price *= 1.25;
                else if (currency.equals("₹")) price *= 0.012; // INR to USD
                
                return Math.round(price * 100.0) / 100.0;
            } catch (NumberFormatException e) {
                System.err.println("❌ Could not parse price: " + priceStr);
            }
        }
        
        // Fallback pattern for numbers without currency symbols
        Pattern numberPattern = Pattern.compile("([0-9]+(?:[.,][0-9]{1,2})?)");
        Matcher numberMatcher = numberPattern.matcher(cleanText);
        
        if (numberMatcher.find()) {
            try {
                double price = Double.parseDouble(numberMatcher.group(1).replace(",", "."));
                return Math.round(price * 100.0) / 100.0;
            } catch (NumberFormatException e) {
                System.err.println("❌ Could not parse number: " + numberMatcher.group(1));
            }
        }
        
        return 0.0;
    }
    
    /**
     * Clears the price cache for fresh price fetching
     */
    public void clearPriceCache() {
        priceCache.clear();
        System.out.println("🗑️ Price cache cleared - fresh prices will be fetched from Steam Market");
    }
    
    /**
     * Gets cache size for monitoring
     */
    public int getCacheSize() {
        return priceCache.size();
    }
}

