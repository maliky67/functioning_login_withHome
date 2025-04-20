package com.example.functioninglogin.HomePageUIClasses.GiftManagment;

import android.text.TextUtils;
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberDataClass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GiftList {
    private String listTitle;
    private double totalBudget;// This is now Total Budget
    private String listImage;
    private String listId;
    private long timestamp;
    private HashMap<String, MemberDataClass> members;
    private String formattedMemberPreview;

    public GiftList() {}

    public GiftList(String listTitle, double totalBudget, String listImage, long timestamp) {
        this.listTitle = listTitle;
        this.totalBudget = totalBudget;
        this.listImage = listImage;
        this.timestamp = timestamp;
        this.members = new HashMap<>();
    }

    public String getListTitle() { return listTitle; }
    public void setListTitle(String listTitle) { this.listTitle = listTitle; }

    public double getTotalBudget() {
        return totalBudget;
    }
    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }
    public String getListImage() { return listImage; }
    public void setListImage(String listImage) { this.listImage = listImage; }

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public HashMap<String, MemberDataClass> getMembers() { return members; }
    public void setMembers(HashMap<String, MemberDataClass> members) { this.members = members; }

    public List<String> getMemberNamesList() {
        List<String> names = new ArrayList<>();
        if (members != null) {
            for (MemberDataClass m : members.values()) {
                names.add(m.getName());
            }
        }
        return names;
    }
    public void setFormattedMemberPreview(String formattedMemberPreview) {
        this.formattedMemberPreview = formattedMemberPreview;
    }
    public String getFormattedMemberPreview() {
        List<String> names = getMemberNamesList();
        int maxToShow = 3;
        if (names.isEmpty()) return "No members";

        List<String> preview = names.subList(0, Math.min(names.size(), maxToShow));
        int remaining = names.size() - preview.size();
        String result = TextUtils.join(", ", preview);
        if (remaining > 0) result += ", +" + remaining;

        return result;
    }
}
