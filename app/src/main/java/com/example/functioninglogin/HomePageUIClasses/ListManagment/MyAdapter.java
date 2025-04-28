package com.example.functioninglogin.HomePageUIClasses.ListManagment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftList;
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberDataClass;
import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
        holder.recDesc.setText(list.getFormattedMemberPreview());

        double spent = calculateTotalSpent(list);
        String totalSpentStr = String.format(Locale.US, "$%.2f", spent);
        String totalBudgetStr = String.format(Locale.US, "$%.2f", list.getTotalBudget());

        holder.totalSpent.setText("Total Spent: " + totalSpentStr);
        holder.totalBudget.setText("Total Budget: " + totalBudgetStr);

        String imageUrl = list.getListImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.recImage);
        } else {
            holder.recImage.setImageResource(R.drawable.hatv1);
        }

        // 🛎️ Check overbudget and adjust visuals
        if (spent > list.getTotalBudget()) {
            // Card background light red
            holder.recCard.setCardBackgroundColor(context.getResources().getColor(R.color.overbudget_background));
            // Text color red
            holder.totalSpent.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.recTitle.setTextColor(context.getResources().getColor(R.color.christmas_blue));
            holder.recDesc.setTextColor(context.getResources().getColor(R.color.christmas_blue));
            holder.totalBudget.setTextColor(context.getResources().getColor(R.color.christmas_blue));
        } else {
            // Reset to normal if not over budget
            holder.recCard.setCardBackgroundColor(context.getResources().getColor(R.color.white)); // your normal background
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
                        String status = gift.getStatus() != null ? gift.getStatus().toLowerCase() : "";
                        if (status.equals("bought") || status.equals("wrapped") || status.equals("arrived")) {
                            try {
                                total += Double.parseDouble(gift.getPrice());
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        return total;
    }

    private void sendOverBudgetNotification(String listTitle, double spent, double budget) {
        String channelId = "over_budget_channel";
        String channelName = "Over Budget Alerts";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications when a list goes over budget");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_access_alarms_24) // ⚡ Make sure you have a notification icon here
                .setContentTitle("Over Budget!")
                .setContentText(String.format(Locale.US, "%s list spent $%.2f / budget $%.2f", listTitle, spent, budget))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        int notificationId = listTitle.hashCode(); // Unique per list
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
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
