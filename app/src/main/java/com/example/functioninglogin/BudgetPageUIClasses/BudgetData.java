package com.example.functioninglogin.BudgetPageUIClasses;

public class BudgetData {
    private String memberName;
    private String memberRole;
    private String memberImageUrl;
    private double totalPrice;

    public BudgetData() {}

    public BudgetData(String memberName, String memberRole, String memberImageUrl, double totalPrice) {
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.memberImageUrl = memberImageUrl;
        this.totalPrice = totalPrice;
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
}
