package com.example.functioninglogin.HomePageUIClasses;

public class MemberDetailsClass {
    private String giftIdea;
    private String price;
    private String website;
    private String notes;
    private String preferences;
    private String status;

    public MemberDetailsClass() {}

    public MemberDetailsClass(String preferences, String giftIdea, String price, String website, String notes, String status) {
        this.preferences = preferences;
        this.giftIdea = giftIdea;
        this.price = price;
        this.website = website;
        this.notes = notes;
        this.status = status;
    }

    public String getGiftIdea() { return giftIdea; }
    public String getPrice() { return price; }
    public String getWebsite() { return website; }
    public String getNotes() { return notes; }
    public String getPreferences() { return preferences; }
    public String getStatus() { return status; }

    public void setGiftIdea(String giftIdea) { this.giftIdea = giftIdea; }
    public void setPrice(String price) { this.price = price; }
    public void setWebsite(String website) { this.website = website; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
    public void setStatus(String status) { this.status = status; }
}
