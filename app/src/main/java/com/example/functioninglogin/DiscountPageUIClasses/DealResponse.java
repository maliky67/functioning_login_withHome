package com.example.functioninglogin.DiscountPageUIClasses;

import java.util.List;

public class DealResponse {
    private DealData data;

    public DealData getData() {
        return data;
    }

    public static class DealData {
        private List<DealItem> deals;

        public List<DealItem> getDeals() {
            return deals;
        }
    }
}
