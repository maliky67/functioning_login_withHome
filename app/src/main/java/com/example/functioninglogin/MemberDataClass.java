package com.example.functioninglogin;

public class MemberDataClass {
    private String name;
    private String role;
    private String imageUrl;
    private String giftIdea;
    private String price;
    private String key;

    public MemberDataClass() {}

    public MemberDataClass(String name, String role, String imageUrl, String giftIdea, String price) {
        this.name = name;
        this.role = role;
        this.imageUrl = imageUrl;
        this.giftIdea = giftIdea;
        this.price = price;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public String getImageUrl() { return imageUrl; }
    public String getGiftIdea() { return giftIdea; }
    public String getPrice() { return price; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setGiftIdea(String giftIdea) { this.giftIdea = giftIdea; }
    public void setPrice(String price) { this.price = price; }
}
