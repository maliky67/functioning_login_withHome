package com.example.functioninglogin.DiscountPage;

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
    private final OnDealClickListener listener;

    public interface OnDealClickListener {
        void onDealClick(DealItem deal);
    }

    public DealsAdapter(OnDealClickListener listener) {
        this.listener = listener;
    }

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

        // ✅ Safely parse the price string to double
        double priceAmount = 0.0;
        if (deal.getDeal_price() != null && deal.getDeal_price().getAmount() != null) {
            try {
                priceAmount = Double.parseDouble(deal.getDeal_price().getAmount());
            } catch (NumberFormatException e) {
                priceAmount = 0.0;
            }
        }

        String formattedPrice = String.format("$%.2f", priceAmount);
        holder.price.setText(formattedPrice);

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

        holder.itemView.setOnClickListener(v -> listener.onDealClick(deal));
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