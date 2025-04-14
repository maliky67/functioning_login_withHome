package com.example.functioninglogin.HomePageUIClasses;

public class GiftItem {
    private String name;
    private String price;
    private String website;
    private String notes;
    private String status;
    private String key;

    // Required empty constructor for Firebase
    public GiftItem() {
        this.name = "";
        this.price = "";
        this.website = "";
        this.notes = "";
        this.status = "Idea"; // Default status
    }

    // Optional constructor for quick add
    public GiftItem(String name, String price) {
        this.name = name;
        this.price = price;
        this.website = "";
        this.notes = "";
        this.status = "Idea";
    }

    // Fully parameterized constructor (use if needed)
    public GiftItem(String name, String price, String website, String notes, String status) {
        this.name = name;
        this.price = price;
        this.website = website;
        this.notes = notes;
        this.status = status;
    }

    // Getters
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getWebsite() { return website; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public String getKey() { return key; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setWebsite(String website) { this.website = website; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(String status) { this.status = status; }
    public void setKey(String key) { this.key = key; }
}
