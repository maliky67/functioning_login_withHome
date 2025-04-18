package com.example.functioninglogin.HomePageUIClasses.MemberManagment;

import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;

import java.util.HashMap;
import java.util.Map;

public class MemberDataClass {
    private String name;
    private String role;
    private String imageUrl;
    private String key; // Firebase member ID
    private Map<String, GiftItem> gifts = new HashMap<>();

    public MemberDataClass() {}

    public MemberDataClass(String name, String role, String imageUrl) {
        this.name = name;
        this.role = role;
        this.imageUrl = imageUrl;
        this.gifts = new HashMap<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public Map<String, GiftItem> getGifts() {
        return gifts;
    }

    public void setGifts(Map<String, GiftItem> gifts) {
        this.gifts = (gifts != null) ? gifts : new HashMap<>();
    }
}
