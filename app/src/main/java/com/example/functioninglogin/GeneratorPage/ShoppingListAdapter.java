package com.example.functioninglogin.GeneratorPage;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingViewHolder> {

    private final List<ShoppingListItem> shoppingList;

    public ShoppingListAdapter(List<ShoppingListItem> shoppingList) {
        this.shoppingList = shoppingList;
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_item, parent, false);
        return new ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        ShoppingListItem item = shoppingList.get(position);

        holder.itemName.setText("🎁 " + item.getGiftName());
        holder.memberName.setText("🎅 For: " + item.getMemberName());
        holder.itemPrice.setText("$" + item.getPrice());

        // Treat "bought", "arrived", or "wrapped" as purchased
        boolean isPurchased = item.getStatus().equalsIgnoreCase("bought") ||
                item.getStatus().equalsIgnoreCase("arrived") ||
                item.getStatus().equalsIgnoreCase("wrapped");
        holder.itemCheck.setChecked(isPurchased);

        // Visual feedback (strike-through)
        holder.itemName.setPaintFlags(isPurchased
                ? holder.itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : holder.itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        // Reset the listener to avoid multiple listeners stacking
        holder.itemCheck.setOnCheckedChangeListener(null);
        holder.itemCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "bought" : "idea";

            // Local visual change
            holder.itemName.setPaintFlags(isChecked
                    ? holder.itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : holder.itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            // Local state update
            item.setStatus(newStatus);

            // Firebase Update
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            FirebaseDatabase.getInstance()
                    .getReference("Unique User ID")
                    .child(userId)
                    .child("lists")
                    .child(item.getListId())
                    .child("members")
                    .child(item.getMemberId())
                    .child("gifts")
                    .child(item.getGiftId())
                    .child("status")
                    .setValue(newStatus);

            Toast.makeText(holder.itemView.getContext(),
                    isChecked ? "Marked as Purchased" : "Marked as Idea", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, memberName, itemPrice;
        CheckBox itemCheck;

        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            memberName = itemView.findViewById(R.id.memberName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemCheck = itemView.findViewById(R.id.itemCheck);
        }
    }
}