package com.example.functioninglogin.HomePageUIClasses.GiftManagment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.R;

import java.util.List;

public class GiftPagerAdapter extends RecyclerView.Adapter<GiftPagerAdapter.GiftViewHolder> {

    private final List<GiftItem> giftList;
    private final Runnable onAddGiftCallback;

    private RecyclerView recyclerView;

    public GiftPagerAdapter(List<GiftItem> giftList, Runnable onAddGiftCallback) {
        this.giftList = giftList;
        this.onAddGiftCallback = onAddGiftCallback;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_gift_fragment, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        if (position >= giftList.size()) return;

        GiftItem gift = giftList.get(position);

        if (gift.getName() != null) holder.giftName.setText(gift.getName());
        if (gift.getPrice() != null) holder.giftPrice.setText(gift.getPrice());
        if (gift.getWebsite() != null) holder.giftWebsite.setText(gift.getWebsite());
        if (gift.getNotes() != null) holder.giftNotes.setText(gift.getNotes());

        if (gift.getStatus() != null) {
            switch (gift.getStatus().toLowerCase()) {
                case "bought":
                    holder.statusGroup.check(R.id.radioBought);
                    break;
                case "arrived":
                    holder.statusGroup.check(R.id.radioArrived);
                    break;
                case "wrapped":
                    holder.statusGroup.check(R.id.radioWrapped);
                    break;
                default:
                    holder.statusGroup.check(R.id.radioIdea);
            }
        }

        holder.giftName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                gift.setName(holder.giftName.getText().toString().trim());
        });

        holder.giftPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                gift.setPrice(holder.giftPrice.getText().toString().trim());
        });

        holder.giftWebsite.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                gift.setWebsite(holder.giftWebsite.getText().toString().trim());
        });

        holder.giftNotes.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                gift.setNotes(holder.giftNotes.getText().toString().trim());
        });

        holder.statusGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String status = "Idea";
            if (checkedId == R.id.radioBought) status = "Bought";
            else if (checkedId == R.id.radioArrived) status = "Arrived";
            else if (checkedId == R.id.radioWrapped) status = "Wrapped";
            gift.setStatus(status);
        });

        // Handle add gift button
        holder.addGiftButton.setOnClickListener(v -> {
            if (onAddGiftCallback != null) onAddGiftCallback.run();
        });
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void updateAllGiftItemsFromUI() {
        if (recyclerView == null) return;

        for (int i = 0; i < giftList.size(); i++) {
            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(i);
            if (vh instanceof GiftViewHolder) {
                GiftViewHolder holder = (GiftViewHolder) vh;
                GiftItem item = giftList.get(i);

                item.setName(holder.giftName.getText().toString().trim());
                item.setPrice(holder.giftPrice.getText().toString().trim());
                item.setWebsite(holder.giftWebsite.getText().toString().trim());
                item.setNotes(holder.giftNotes.getText().toString().trim());

                int checkedId = holder.statusGroup.getCheckedRadioButtonId();
                String status = "Idea";
                if (checkedId == R.id.radioBought) status = "Bought";
                else if (checkedId == R.id.radioArrived) status = "Arrived";
                else if (checkedId == R.id.radioWrapped) status = "Wrapped";

                item.setStatus(status);
            }
        }
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        EditText giftName, giftPrice, giftWebsite, giftNotes;
        RadioGroup statusGroup;
        Button addGiftButton;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            giftName = itemView.findViewById(R.id.giftNameEdit);
            giftPrice = itemView.findViewById(R.id.giftPriceEdit);
            giftWebsite = itemView.findViewById(R.id.giftWebsiteEdit);
            giftNotes = itemView.findViewById(R.id.giftNotesEdit);
            statusGroup = itemView.findViewById(R.id.statusGroup);
            addGiftButton = itemView.findViewById(R.id.saveGiftButton); // 👈 matches your XML
        }
    }
}
