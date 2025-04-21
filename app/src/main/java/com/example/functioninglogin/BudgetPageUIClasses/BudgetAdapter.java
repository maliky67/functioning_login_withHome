package com.example.functioninglogin.BudgetPageUIClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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

        // Fallbacks
        String name = data.getMemberName() != null ? data.getMemberName() : "No Name";
        double totalSpent = data.getTotalPrice();
        double totalBudget = data.getTotalBudget();

        // Set values
        holder.nameTextView.setText(name);
        holder.totalTextView.setText(String.format("Gift Total: $%.2f", totalSpent));
        holder.maxTextView.setText(String.format("Budget: $%.2f", totalBudget));
        holder.memberImageView.setImageResource(R.drawable.baseline_account_box_24); // can update to URL later

        // Progress logic
        float progress = data.getProgressPercentage(); // 0–100
        holder.progressBar.setProgress((int) progress);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void setBudgetList(List<BudgetData> newList) {
        this.budgetList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView memberImageView;
        TextView nameTextView, totalTextView, maxTextView;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImageView = itemView.findViewById(R.id.memberImage);
            nameTextView = itemView.findViewById(R.id.budgetCardName);
            totalTextView = itemView.findViewById(R.id.budgetCardTotal);
            maxTextView = itemView.findViewById(R.id.budgetCardMax);
            progressBar = itemView.findViewById(R.id.budgetProgressBar);
        }
    }
}
