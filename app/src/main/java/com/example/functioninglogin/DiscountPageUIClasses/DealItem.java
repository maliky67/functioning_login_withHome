package com.example.functioninglogin.DiscountPageUIClasses;

public class DealItem {
    private String title;
    private String price;
    private String link;

    public DealItem(String title, String price, String link) {
        this.title = title;
        this.price = price;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getLink() {
        return link;
    }
}

