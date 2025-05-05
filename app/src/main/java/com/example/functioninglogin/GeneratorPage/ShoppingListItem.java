package com.example.functioninglogin.GeneratorPage;

public class ShoppingListItem {
    private String listId;
    private String memberId;
    private String giftId;
    private String memberName;
    private String giftName;
    private String price;
    private String status;

    public ShoppingListItem(String listId, String memberId, String giftId, String memberName, String giftName, String price, String status) {
        this.listId = listId;
        this.memberId = memberId;
        this.giftId = giftId;
        this.memberName = memberName;
        this.giftName = giftName;
        this.price = price;
        this.status = status;
    }

    public String getListId() {
        return listId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getGiftId() {
        return giftId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getGiftName() {
        return giftName;
    }

    public String getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
