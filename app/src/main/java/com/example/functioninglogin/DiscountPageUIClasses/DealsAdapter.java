package com.example.functioninglogin.DiscountPageUIClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.DiscountPageUIClasses.DealItem;
import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.List;

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.DealViewHolder> {

    private List<DealItem> dealList = new ArrayList<>();

    public void setDealList(List<DealItem> deals) {
        this.dealList = deals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deal, parent, false);
        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        DealItem deal = dealList.get(position);
        holder.title.setText(deal.getTitle());
        holder.price.setText(deal.getPrice());
        holder.link.setText(deal.getLink());
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, link;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dealTitle);
            price = itemView.findViewById(R.id.dealPrice);
            link = itemView.findViewById(R.id.dealLink);
        }
    }
}
