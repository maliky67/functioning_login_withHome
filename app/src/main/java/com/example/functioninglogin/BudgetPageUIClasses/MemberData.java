package com.example.functioninglogin.BudgetPageUIClasses;

import com.example.functioninglogin.HomePageUIClasses.GiftItem;

import java.util.List;

public class MemberData {
    private String name;
    private String role;
    private String imageUrl;
    private List<GiftItem> giftList;

    public MemberData(String name, String role, String imageUrl, List<GiftItem> giftList) {
        this.name = name;
        this.role = role;
        this.imageUrl = imageUrl;
        this.giftList = giftList;
    }

    public String getName() { return name; }

    public String getRole() { return role; }

    public String getImageUrl() { return imageUrl; }

    public List<GiftItem> getGiftList() { return giftList; }
}
