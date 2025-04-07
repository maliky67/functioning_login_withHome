package com.example.functioninglogin.HomePageUIClasses;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

class MyViewHolder extends RecyclerView.ViewHolder{

    ImageView recImage;
    TextView recTitle, recDesc;
    CardView recCard;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recDesc = itemView.findViewById(R.id.recDesc);
        recTitle = itemView.findViewById(R.id.recTitle);
        if (recTitle == null || recDesc == null) {
            throw new RuntimeException("View IDs are incorrect. Check recycler_item.xml.");
        }
    }
}

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final Context context;
    private List<DataHelperClass> dataList;

    public MyAdapter(Context context, List<DataHelperClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        DataHelperClass data = dataList.get(position);

        if (data == null) {
            return; // Skip binding if data is null
        }

        if (data.getDataTitle() != null) {
            holder.recTitle.setText(data.getDataTitle());
        } else {
            holder.recTitle.setText("No Title");
        }

        if (data.getDataDesc() != null) {
            holder.recDesc.setText(data.getDataDesc());
        } else {
            holder.recDesc.setText("No Description");
        }

        // Load image with Glide
        if (data.getDataImage() != null) {
            Glide.with(context).load(data.getDataImage()).into(holder.recImage);
        } else {
            holder.recImage.setImageResource(R.drawable.hatv1); // Use a default image
        }
        Glide.with(context).load(dataList.get(position).getDataImage()).into(holder.recImage);
        holder.recTitle.setText(dataList.get(position).getDataTitle());
        holder.recDesc.setText(dataList.get(position).getDataDesc());
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ListViewPage.class);
                intent.putExtra("Image", dataList.get(holder.getAdapterPosition()).getDataImage());
                intent.putExtra("Description", dataList.get(holder.getAdapterPosition()).getDataDesc());
                intent.putExtra("Title", dataList.get(holder.getAdapterPosition()).getDataTitle());
                intent.putExtra("Key",dataList.get(holder.getAdapterPosition()).getKey());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void searchDataList(ArrayList<DataHelperClass> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
}

