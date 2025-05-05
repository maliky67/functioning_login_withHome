package com.example.functioninglogin.HomePageUIClasses.GiftManagment;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;

import java.util.List;

public class GiftDisplayAdapter extends RecyclerView.Adapter<GiftDisplayAdapter.GiftViewHolder> {

    private final Context context;
    private final List<GiftItem> giftList;
    private final String listKey;
    private final String memberKey;

    public GiftDisplayAdapter(Context context, List<GiftItem> giftList, String listKey, String memberKey) {
        this.context = context;
        this.giftList = giftList;
        this.listKey = listKey;
        this.memberKey = memberKey;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gift_recycler_item, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftItem gift = giftList.get(position);

        holder.giftName.setText(gift.getName());
        holder.giftUrl.setText(gift.getWebsite());
        holder.giftPrice.setText("$" + gift.getPrice());


        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(gift.getImageUrl())
                    .placeholder(R.drawable.baseline_card_giftcard_24) // Placeholder while loading
                    .error(R.drawable.baseline_clear_24) // If URL is broken
                    .into(holder.giftImage);
        } else {
            holder.giftImage.setImageResource(R.drawable.baseline_ac_unit_24);
        }

        // Set gift status image based on status
        String status = gift.getStatus();
        if (status != null) {
            switch (status.toLowerCase()) {
                case "bought":
                    holder.giftStatus.setImageResource(R.drawable.baseline_attach_money_24);
                    break;
                case "arrived":
                    holder.giftStatus.setImageResource(R.drawable.baseline_check_24);
                    break;
                case "wrapped":
                    holder.giftStatus.setImageResource(R.drawable.baseline_card_giftcard_24);
                    break;
                default:
                    holder.giftStatus.setImageResource(R.drawable.baseline_lightbulb_24);
            }
        }

        // 🧠 On Click -> Launch EditGiftFragment
        holder.itemView.setOnClickListener(v -> {
            if (gift.getKey() != null) {
                EditGiftFragment fragment = EditGiftFragment.newInstance(listKey, memberKey, gift.getKey());

                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    static class GiftViewHolder extends RecyclerView.ViewHolder {
        TextView giftName, giftUrl, giftPrice;
        ImageView giftStatus,giftImage;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            giftName = itemView.findViewById(R.id.giftName);
            giftUrl = itemView.findViewById(R.id.giftURL);
            giftPrice = itemView.findViewById(R.id.giftPrice);
            giftStatus = itemView.findViewById(R.id.giftStatus);
            giftImage = itemView.findViewById(R.id.giftImage);       // ✅ New image view

        }
    }
}
