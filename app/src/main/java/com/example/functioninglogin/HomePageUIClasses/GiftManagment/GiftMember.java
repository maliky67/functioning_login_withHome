package com.example.functioninglogin.HomePageUIClasses.GiftManagment;
import java.util.ArrayList;
import java.util.List;

public class GiftMember {
    public String name;
    public String role;
    public List<GiftItem> gifts = new ArrayList<>();

    public GiftMember() {}

    public GiftMember(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public void addGift(GiftItem gift) {
        gifts.add(gift);
    }
}
