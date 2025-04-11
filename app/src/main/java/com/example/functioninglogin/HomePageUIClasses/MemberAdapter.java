package com.example.functioninglogin.HomePageUIClasses;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;

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
            DetailFragment fragment = DetailFragment.newInstance(
                    member.getKey(),
                    listKey,
                    "member",
                    member.getImageUrl()
            );

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
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
