package com.example.functioninglogin.DiscountPageUIClasses;

import android.os.Parcel;
import android.os.Parcelable;

public class DealItem implements Parcelable {

    private String deal_title;
    private String deal_photo;
    private String deal_url;
    private DealPrice deal_price;
    private DealPrice list_price;
    private int savings_percentage;
    private String deal_badge;

    public DealItem() {}

    protected DealItem(Parcel in) {
        deal_title = in.readString();
        deal_photo = in.readString();
        deal_url = in.readString();
        deal_price = in.readParcelable(DealPrice.class.getClassLoader());
        list_price = in.readParcelable(DealPrice.class.getClassLoader());
        savings_percentage = in.readInt();
        deal_badge = in.readString();
    }

    public static final Creator<DealItem> CREATOR = new Creator<DealItem>() {
        @Override
        public DealItem createFromParcel(Parcel in) {
            return new DealItem(in);
        }

        @Override
        public DealItem[] newArray(int size) {
            return new DealItem[size];
        }
    };

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

    public String getDescription() {
        return "Savings: " + savings_percentage + "% off";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(deal_title);
        parcel.writeString(deal_photo);
        parcel.writeString(deal_url);
        parcel.writeParcelable(deal_price, i);
        parcel.writeParcelable(list_price, i);
        parcel.writeInt(savings_percentage);
        parcel.writeString(deal_badge);
    }

    public static class DealPrice implements Parcelable {
        private String amount;

        public DealPrice() {}

        protected DealPrice(Parcel in) {
            amount = in.readString();
        }

        public static final Creator<DealPrice> CREATOR = new Creator<DealPrice>() {
            @Override
            public DealPrice createFromParcel(Parcel in) {
                return new DealPrice(in);
            }

            @Override
            public DealPrice[] newArray(int size) {
                return new DealPrice[size];
            }
        };

        public String getAmount() {
            return amount;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(amount);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
