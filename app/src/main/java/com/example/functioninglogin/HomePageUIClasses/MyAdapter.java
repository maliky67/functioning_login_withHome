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

class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView recImage;
    TextView recTitle, recDesc;
    CardView recCard;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recImage = itemView.findViewById(R.id.recImage);
        recTitle = itemView.findViewById(R.id.recTitle);
        recDesc = itemView.findViewById(R.id.recDesc);
        recCard = itemView.findViewById(R.id.recCard);
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
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataHelperClass data = dataList.get(position);

        holder.recTitle.setText(data.getDataTitle() != null ? data.getDataTitle() : "No Title");
        holder.recDesc.setText(data.getDataDesc() != null ? data.getDataDesc() : "No Description");

        // ✅ Load image using fallback-compatible URL getter
        String imageUrl = data.getDataImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.recImage);
        } else {
            holder.recImage.setImageResource(R.drawable.hatv1);
        }

        holder.recCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, ListViewPage.class);
            intent.putExtra("Image", imageUrl != null ? imageUrl : "");
            intent.putExtra("Description", data.getDataDesc());
            intent.putExtra("Title", data.getDataTitle());
            intent.putExtra("Key", data.getKey());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public void searchDataList(ArrayList<DataHelperClass> searchList) {
        this.dataList = searchList;
        notifyDataSetChanged();
    }
}
