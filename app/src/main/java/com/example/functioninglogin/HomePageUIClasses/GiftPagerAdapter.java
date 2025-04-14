package com.example.functioninglogin.HomePageUIClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GiftPagerAdapter extends RecyclerView.Adapter<GiftPagerAdapter.GiftViewHolder> {

    private final List<GiftItem> giftList;
    private final String listKey;
    private final String memberKey;
    private final Runnable onAddGiftCallback;

    public GiftPagerAdapter(List<GiftItem> giftList, String listKey, String memberKey, Runnable onAddGiftCallback) {
        this.giftList = giftList;
        this.listKey = listKey;
        this.memberKey = memberKey;
        this.onAddGiftCallback = onAddGiftCallback;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gift_form, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftItem gift = giftList.get(position);

        // Load saved data
        holder.giftName.setText(gift.getName());
        holder.giftPrice.setText(gift.getPrice());
        holder.giftWebsite.setText(gift.getWebsite());
        holder.giftNotes.setText(gift.getNotes());

        // Select correct status
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

        // Save on focus loss
        holder.giftName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) gift.setName(holder.giftName.getText().toString().trim());
        });

        holder.giftPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) gift.setPrice(holder.giftPrice.getText().toString().trim());
        });

        holder.giftWebsite.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) gift.setWebsite(holder.giftWebsite.getText().toString().trim());
        });

        holder.giftNotes.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) gift.setNotes(holder.giftNotes.getText().toString().trim());
        });

        holder.statusGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String status = "Idea";
            if (checkedId == R.id.radioBought) status = "Bought";
            else if (checkedId == R.id.radioArrived) status = "Arrived";
            else if (checkedId == R.id.radioWrapped) status = "Wrapped";

            gift.setStatus(status); // ✅ Update in model
        });

        // Add Gift
        holder.addFab.setOnClickListener(v -> {
            if (onAddGiftCallback != null) onAddGiftCallback.run();
        });

        // Delete Gift
        holder.deleteButton.setOnClickListener(v -> {
            if (gift.getKey() != null) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance()
                        .getReference("Unique User ID")
                        .child(userId)
                        .child(listKey)
                        .child("members")
                        .child(memberKey)
                        .child("gifts")
                        .child(gift.getKey())
                        .removeValue();
            }

            giftList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, giftList.size());

            if (giftList.isEmpty()) {
                giftList.add(new GiftItem());
                notifyItemInserted(0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    private RecyclerView recyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void updateAllGiftItemsFromUI() {
        for (int i = 0; i < giftList.size(); i++) {
            GiftViewHolder holder = (GiftViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
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
        Button deleteButton;
        FloatingActionButton addFab;
        RadioGroup statusGroup;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            giftName = itemView.findViewById(R.id.giftNameEdit);
            giftPrice = itemView.findViewById(R.id.giftPriceEdit);
            giftWebsite = itemView.findViewById(R.id.giftWebsiteEdit);
            giftNotes = itemView.findViewById(R.id.giftNotesEdit);
            deleteButton = itemView.findViewById(R.id.deleteGiftFab);
            addFab = itemView.findViewById(R.id.addGiftFab);
            statusGroup = itemView.findViewById(R.id.statusGroup);
        }
    }
}
