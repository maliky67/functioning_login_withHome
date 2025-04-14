package com.example.functioninglogin.HomePageUIClasses;

import java.util.HashMap;

public class GiftList {

    private String listTitle;
    private String listDesc;
    private String listImage;
    private String listId;
    private long timestamp;

    // ✅ FIXED TYPE: was `HashMap<String, Member>` (crash) 🔥
    private HashMap<String, MemberDataClass> members;

    public GiftList() {
        // Firebase needs empty constructor
    }

    public GiftList(String listTitle, String listDesc, String listImage, long timestamp) {
        this.listTitle = listTitle;
        this.listDesc = listDesc;
        this.listImage = listImage;
        this.timestamp = timestamp;
        this.members = new HashMap<>();
    }

    public String getListTitle() {
        return listTitle;
    }

    public void setListTitle(String listTitle) {
        this.listTitle = listTitle;
    }

    public String getListDesc() {
        return listDesc;
    }

    public void setListDesc(String listDesc) {
        this.listDesc = listDesc;
    }

    public String getListImage() {
        return listImage;
    }

    public void setListImage(String listImage) {
        this.listImage = listImage;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ✅ FIXED getter/setter
    public HashMap<String, MemberDataClass> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, MemberDataClass> members) {
        this.members = members;
    }
}
