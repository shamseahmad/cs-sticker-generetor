package com.example.stickergenerator.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NameRequest {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 1, max = 20, message = "Name must be between 1 and 20 characters")
    private String name;
    
    private String sortOrder = "asc"; // "asc" or "desc"
    
    public NameRequest() {}
    
    public NameRequest(String name, String sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}
