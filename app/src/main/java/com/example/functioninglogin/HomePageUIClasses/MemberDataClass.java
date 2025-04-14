package com.example.functioninglogin.HomePageUIClasses;

import java.util.HashMap;
import java.util.Map;

public class MemberDataClass {
    private String name;
    private String role;
    private String imageUrl;
    private String giftIdea;
    private String price;
    private String key;
    private Map<String, GiftItem> gifts = new HashMap<>();

    public MemberDataClass() {}

    public MemberDataClass(String name, String role, String imageUrl) {
        this.name = name;
        this.role = role;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getGiftIdea() { return giftIdea; }
    public void setGiftIdea(String giftIdea) { this.giftIdea = giftIdea; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    // ✅ Correct map-based gift field
    public Map<String, GiftItem> getGifts() {
        return gifts;
    }

    public void setGifts(Map<String, GiftItem> gifts) {
        this.gifts = gifts;
    }
}
