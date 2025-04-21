package com.example.functioninglogin.DiscountPageUIClasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.List;

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.DealViewHolder> {

    private final List<DealItem> dealList = new ArrayList<>();

    public void updateDeals(List<DealItem> deals) {
        dealList.clear();
        if (deals != null) {
            dealList.addAll(deals);
        }
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

        holder.title.setText(deal.getDeal_title() != null ? deal.getDeal_title() : "No title");

        String price = (deal.getDeal_price() != null && deal.getDeal_price().getAmount() != null)
                ? "$" + deal.getDeal_price().getAmount()
                : "Price N/A";
        holder.price.setText(price);

        holder.link.setText(deal.getDeal_url() != null ? deal.getDeal_url() : "No link");

        String imageUrl = deal.getDeal_photo();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_attach_money_24)
                    .error(R.drawable.baseline_clear_24)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.baseline_ac_unit_24);
        }
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, link;
        ImageView image;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dealTitle);
            price = itemView.findViewById(R.id.dealPrice);
            link = itemView.findViewById(R.id.dealLink);
            image = itemView.findViewById(R.id.dealImage);
        }
    }
}
