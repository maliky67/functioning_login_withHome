package com.example.functioninglogin.HomePageUIClasses.GiftManagment;

public class GiftItem {
    private String name;
    private String notes;
    private String price;
    private String status;
    private String website;
    private String key;       // 🔑 Firebase key
    private String imageUrl;  // 🖼️ New: Image URL

    public GiftItem() {}
    public GiftItem(String name, String notes, String price, String status, String website) {
        this.name = name;
        this.notes = notes;
        this.price = price;
        this.status = status;
        this.website = website;
        this.imageUrl = ""; // default empty string to avoid nulls
    }

    public GiftItem(String name, String notes, String price, String status, String website, String imageUrl) {
        this.name = name;
        this.notes = notes;
        this.price = price;
        this.status = status;
        this.website = website;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    // 🖼️ Getter & Setter for Image URL
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
