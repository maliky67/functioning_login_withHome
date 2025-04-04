package com.example.functioninglogin;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePageUIClasses.DetailActivity;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final Context context;
    private final List<MemberDataClass> members;
    private final String listKey;

    public MemberAdapter(Context context, List<MemberDataClass> members, String listKey) {
        this.context = context;
        this.members = members;
        this.listKey = listKey;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.member_recycler_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        MemberDataClass member = members.get(position);

        updateTextAnimated(holder.name, member.getName());
        updateTextAnimated(holder.role, member.getRole());
        updateTextAnimated(holder.price, "$" + member.getPrice());

        if (member.getImageUrl() != null && !member.getImageUrl().isEmpty()) {
            Glide.with(context).load(member.getImageUrl()).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.baseline_ac_unit_24);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("type", "member");
            intent.putExtra("Title", member.getName());
            intent.putExtra("Description", member.getRole());
            intent.putExtra("GiftIdea", member.getGiftIdea());
            intent.putExtra("Price", member.getPrice());
            intent.putExtra("Image", member.getImageUrl());
            intent.putExtra("Key", member.getKey());
            intent.putExtra("listKey", listKey);
            context.startActivity(intent);
        });
    }

    private void updateTextAnimated(TextView view, String newText) {
        if (!view.getText().toString().equals(newText)) {
            view.setText(newText);
            ObjectAnimator.ofArgb(view, "backgroundColor", Color.YELLOW, Color.TRANSPARENT)
                    .setDuration(600)
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView name, role, price;
        ImageView image;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.memberName);
            role = itemView.findViewById(R.id.memberRole);
            price = itemView.findViewById(R.id.memberPrice);
            image = itemView.findViewById(R.id.memberImage);
        }
    }
}
