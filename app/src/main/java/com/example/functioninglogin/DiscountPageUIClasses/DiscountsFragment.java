package com.example.functioninglogin.DiscountPageUIClasses;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.functioninglogin.R;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DealsAdapter();
        recyclerView.setAdapter(adapter);

        fetchDeals();

        return view;
    }

    private void fetchDeals() {
        DealsApi api = RetrofitInstance.getApi();

        // Real API: this should call "deals-v2" or similar, not product-offers
        Call<DealResponse> call = api.getDeals("US", "ALL", "ALL", 1);

        call.enqueue(new Callback<DealResponse>() {
            @Override
            public void onResponse(@NonNull Call<DealResponse> call, @NonNull Response<DealResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DealItem> deals = response.body().getData().getDeals();
                    adapter.updateDeals(deals);
                } else {
                    Log.e("DEAL_FETCH", "❌ Unexpected response: " + response.code());
                    Toast.makeText(requireContext(), "❌ Failed to load deals", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DealResponse> call, @NonNull Throwable t) {
                Log.e("API", "Failed: " + t.getMessage());
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
