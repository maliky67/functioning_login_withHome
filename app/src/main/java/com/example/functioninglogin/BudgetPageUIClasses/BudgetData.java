package com.example.functioninglogin.BudgetPageUIClasses;

import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;

import java.util.ArrayList;
import java.util.List;

public class BudgetData {
    private String memberName;
    private String memberRole;
    private String memberImageUrl;
    private double totalPrice;
    private List<GiftItem> gifts = new ArrayList<>();
    private int assignedColor;

    // ✅ NEW: total budget assigned to this list
    private double totalBudget;

    public BudgetData() {}

    public BudgetData(String memberName, String memberRole, String memberImageUrl, double totalPrice) {
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.memberImageUrl = memberImageUrl;
        this.totalPrice = totalPrice;
    }

    // ✅ NEW: overloaded constructor to include total budget
    public BudgetData(String memberName, String memberRole, String memberImageUrl, double totalPrice, double totalBudget) {
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.memberImageUrl = memberImageUrl;
        this.totalPrice = totalPrice;
        this.totalBudget = totalBudget;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public String getMemberImageUrl() {
        return memberImageUrl;
    }

    public void setMemberImageUrl(String memberImageUrl) {
        this.memberImageUrl = memberImageUrl;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    // ✅ NEW: list total budget
    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    // ✅ NEW: derived % for UI use
    public float getProgressPercentage() {
        if (totalBudget <= 0) return 0f;
        return (float) ((totalPrice / totalBudget) * 100f);
    }
    public List<GiftItem> getGifts() {
        return gifts;
    }

    public void setGifts(List<GiftItem> gifts) {
        this.gifts = (gifts != null) ? gifts : new ArrayList<>();
    }
    public int getAssignedColor() {
        return assignedColor;
    }

    public void setAssignedColor(int assignedColor) {
        this.assignedColor = assignedColor;
    }
}
