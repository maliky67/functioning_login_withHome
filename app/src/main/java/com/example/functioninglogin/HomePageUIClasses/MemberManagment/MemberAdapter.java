package com.example.functioninglogin.HomePageUIClasses.MemberManagment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.R;

import java.util.List;
import java.util.Map;

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
        updateTextAnimated(holder.role, " " + member.getRole());

        // Load image
        if (member.getImageUrl() != null && !member.getImageUrl().isEmpty()) {
            Glide.with(context).load(member.getImageUrl()).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.baseline_ac_unit_24);
        }

        // Handle gifts
        holder.giftListContainer.removeAllViews();
        Map<String, GiftItem> gifts = member.getGifts();
        double total = 0;

        if (gifts != null && !gifts.isEmpty()) {
            for (GiftItem gift : gifts.values()) {
                try {
                    total += Double.parseDouble(gift.getPrice());
                } catch (NumberFormatException ignored) {}

                TextView giftView = new TextView(context);
                giftView.setText("🎁 " + gift.getName());
                giftView.setTextColor(context.getResources().getColor(R.color.blue));
                giftView.setTextSize(14);
                giftView.setPadding(0, 4, 0, 0);
                holder.giftListContainer.addView(giftView);
            }

            updateTextAnimated(holder.price, "$" + String.format("%.2f", total));
        } else {
            holder.price.setText("");
        }

        // Click to open fragment
        holder.itemView.setOnClickListener(v -> {
            if (member.getKey() != null && !member.getKey().isEmpty()) {
                MemberViewFragment fragment = MemberViewFragment.newInstance(
                        listKey,
                        member.getKey()
                );

                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(context, "⏳ Member not ready. Try again shortly.", Toast.LENGTH_SHORT).show();
                holder.itemView.setAlpha(0.5f); // visual cue it's not active yet
            }
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
        LinearLayout giftListContainer;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.memberName);
            role = itemView.findViewById(R.id.memberRole);
            price = itemView.findViewById(R.id.memberTotalSpent);
            image = itemView.findViewById(R.id.memberImage);
            giftListContainer = itemView.findViewById(R.id.giftListContainer);
        }
    }
}
