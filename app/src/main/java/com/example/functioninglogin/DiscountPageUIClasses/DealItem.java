package com.example.functioninglogin.DiscountPageUIClasses;

import java.io.Serializable;

public class DealItem implements Serializable {

    private String deal_title;
    private String deal_photo;
    private String deal_url;
    private DealPrice deal_price;
    private DealPrice list_price;
    private int savings_percentage;
    private String deal_badge;

    public String getDeal_title() {
        return deal_title;
    }

    public String getDeal_photo() {
        return deal_photo;
    }

    public String getDeal_url() {
        return deal_url;
    }

    public DealPrice getDeal_price() {
        return deal_price;
    }

    public DealPrice getList_price() {
        return list_price;
    }

    public int getSavings_percentage() {
        return savings_percentage;
    }

    public String getDeal_badge() {
        return deal_badge;
    }

    // Add a method to get a description (since the API doesn't provide one, we'll return a placeholder)
    public String getDescription() {
        return "Savings: " + savings_percentage + "% off"; // Placeholder description
    }

    public static class DealPrice {
        private String amount;

        public String getAmount() {
            return amount;
        }
    }
}