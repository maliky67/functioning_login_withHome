package com.example.functioninglogin.DiscountPageUIClasses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.functioninglogin.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscountsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DealsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_discounts, container, false);

        recyclerView = view.findViewById(R.id.dealsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DealsAdapter();
        recyclerView.setAdapter(adapter);

        fetchDeals();

        return view;
    }

    private void fetchDeals() {
        DealsApi api = RetrofitInstance.getApi();
        api.getDeals().enqueue(new Callback<List<DealItem>>() {
            @Override
            public void onResponse(Call<List<DealItem>> call, Response<List<DealItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setDealList(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<DealItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load deals", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
