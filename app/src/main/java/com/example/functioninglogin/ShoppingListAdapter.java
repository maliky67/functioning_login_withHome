package com.example.functioninglogin.BudgetPageUIClasses;

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
import com.example.functioninglogin.ShoppingListItem;

import java.util.List;

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

        holder.itemName.setText("🎁 " + item.getItemName());
        holder.memberName.setText("🎅 For: " + item.getMemberName());
        holder.itemPrice.setText("$" + item.getPrice());

        boolean isBought = item.getStatus().equalsIgnoreCase("bought");
        holder.itemCheck.setChecked(isBought);

        // Visual feedback for bought items
        if (isBought) {
            holder.itemName.setPaintFlags(holder.itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.itemName.setPaintFlags(holder.itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.itemCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.itemName.setPaintFlags(holder.itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                Toast.makeText(holder.itemView.getContext(), "Marked as Purchased", Toast.LENGTH_SHORT).show();

                // TODO: Optional - Update Firebase status to "bought"
            } else {
                holder.itemName.setPaintFlags(holder.itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                Toast.makeText(holder.itemView.getContext(), "Marked as Idea", Toast.LENGTH_SHORT).show();

                // TODO: Optional - Update Firebase status to "idea"
            }
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
