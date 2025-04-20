package com.example.functioninglogin.HomePageUIClasses.ListManagment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftList;
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberDataClass;
import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView recImage;
    TextView recTitle, recDesc, totalSpent, totalBudget;
    CardView recCard;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recImage = itemView.findViewById(R.id.recImage);
        recTitle = itemView.findViewById(R.id.recTitle);
        recDesc = itemView.findViewById(R.id.recDesc);
        totalSpent = itemView.findViewById(R.id.totalSpent);
        totalBudget = itemView.findViewById(R.id.totalBudget);
        recCard = itemView.findViewById(R.id.recCard);
    }
}

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final Context context;
    private List<GiftList> dataList;
    private List<GiftList> fullDataList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GiftList item);
    }

    public MyAdapter(Context context, List<GiftList> dataList, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.fullDataList = new ArrayList<>(dataList);
        this.dataList = new ArrayList<>(dataList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        GiftList list = dataList.get(position);

        holder.recTitle.setText(list.getListTitle() != null ? list.getListTitle() : "No Title");

        // 👥 Members
        holder.recDesc.setText(list.getFormattedMemberPreview());

        // 💰 Budget
        holder.totalBudget.setText("Total Budget: $" + list.getTotalBudget());

        // 💸 Total Spent
        double spent = calculateTotalSpent(list);
        holder.totalSpent.setText("Total Spent: $" + spent);

        // 🖼 Image
        String imageUrl = list.getListImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.recImage);
        } else {
            holder.recImage.setImageResource(R.drawable.hatv1);
        }

        holder.recCard.setOnClickListener(v -> listener.onItemClick(list));
    }

    private double calculateTotalSpent(GiftList list) {
        double total = 0.0;
        HashMap<String, MemberDataClass> members = list.getMembers();
        if (members != null) {
            for (MemberDataClass member : members.values()) {
                if (member.getGifts() != null) {
                    for (GiftItem gift : member.getGifts().values()) {
                        try {
                            total += Double.parseDouble(gift.getPrice());
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return total;
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public void searchDataList(ArrayList<GiftList> searchList) {
        dataList = new ArrayList<>(searchList);
        notifyDataSetChanged();
    }

    public void updateData(List<GiftList> newData) {
        fullDataList.clear();
        fullDataList.addAll(newData);
        dataList = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    public void resetFilter() {
        dataList = new ArrayList<>(fullDataList);
        notifyDataSetChanged();
    }
}
