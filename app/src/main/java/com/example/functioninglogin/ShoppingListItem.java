package com.example.functioninglogin;

public class ShoppingListItem {
    private String memberName;
    private String itemName;
    private String price;
    private String status;

    public ShoppingListItem(String memberName, String itemName, String price, String status) {
        this.memberName = memberName;
        this.itemName = itemName;
        this.price = price;
        this.status = status;
    }

    public String getMemberName() { return memberName; }
    public String getItemName() { return itemName; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
}
