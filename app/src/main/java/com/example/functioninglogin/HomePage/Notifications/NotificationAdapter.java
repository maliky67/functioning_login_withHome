package com.example.functioninglogin.HomePage.Notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationItem> list;

    public NotificationAdapter(List<NotificationItem> list) {
        this.list = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);
        holder.title.setText(item.title);
        holder.message.setText(item.message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
