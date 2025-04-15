package com.example.functioninglogin.BudgetPageUIClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<BudgetData> budgetList;

    public BudgetAdapter(List<BudgetData> budgetList) {
        this.budgetList = budgetList != null ? budgetList : new ArrayList<>();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.budget_card_item, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetData data = budgetList.get(position);
        String name = data.getMemberName() != null ? data.getMemberName() : "No Name";
        double total = data.getTotalPrice();

        holder.nameTextView.setText(name);
        holder.totalTextView.setText(String.format("Total: $%.2f", total));
        holder.memberImageView.setImageResource(R.drawable.baseline_ac_unit_24);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void setBudgetList(List<BudgetData> newList) {
        this.budgetList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView memberImageView;
        TextView nameTextView, totalTextView;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImageView = itemView.findViewById(R.id.memberImage); // <- Make sure this ID matches layout
            nameTextView = itemView.findViewById(R.id.budgetCardName);
            totalTextView = itemView.findViewById(R.id.budgetCardTotal);
        }
    }

}
