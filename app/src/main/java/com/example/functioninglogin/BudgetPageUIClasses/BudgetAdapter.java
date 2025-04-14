package com.example.functioninglogin.BudgetPageUIClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        holder.memberName.setText(name);
        holder.totalAmount.setText(String.format("Total: $%.2f", total));
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void setBudgetList(List<BudgetData> newList) {
        this.budgetList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView memberName, totalAmount;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.budgetCardName);
            totalAmount = itemView.findViewById(R.id.budgetCardTotal);
        }
    }
}
