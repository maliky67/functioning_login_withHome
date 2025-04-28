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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscountsFragment extends Fragment {

    private DealsAdapter adapter;
    private View loadingOverlay;
    private final List<DealItem> cachedDeals = new ArrayList<>(); // Cache for deals
    private long lastFetchTime = 0; // Timestamp of last API call
    private static final long FETCH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes interval

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_discounts, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.dealsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DealsAdapter(deal -> {
            // Navigate to DealDetailsFragment when a deal is clicked
            DealDetailsFragment fragment = DealDetailsFragment.newInstance(deal);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // Load cached deals if available
        if (!cachedDeals.isEmpty()) {
            adapter.updateDeals(cachedDeals);
        }

        // Fetch deals if needed
        fetchDeals();

        return view;
    }

    private void fetchDeals() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFetchTime < FETCH_INTERVAL_MS && !cachedDeals.isEmpty()) {
            adapter.updateDeals(cachedDeals);
            return;
        }

        showLoading(true);

        DealsApi api = RetrofitInstance.getApi();
        Call<DealResponse> call = api.getDeals("US", "ALL", "ALL", 1);

        fetchDealsWithRetry(call, 0);
    }

    private void fetchDealsWithRetry(Call<DealResponse> call, int retryCount) {
        final int MAX_RETRIES = 3;
        final long BASE_DELAY_MS = 60 * 1000; // 1 minute base delay

        call.clone().enqueue(new Callback<DealResponse>() {
            @Override
            public void onResponse(@NonNull Call<DealResponse> call, @NonNull Response<DealResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<DealItem> deals = response.body().getData().getDeals();
                    cachedDeals.clear();
                    cachedDeals.addAll(deals);
                    lastFetchTime = System.currentTimeMillis();
                    adapter.updateDeals(deals);
                } else {
                    if (response.code() == 429 && retryCount < MAX_RETRIES) {
                        // Handle rate limit error with retry
                        long delay = BASE_DELAY_MS * (long) Math.pow(2, retryCount); // Exponential backoff
                        Toast.makeText(requireContext(), "Rate limit exceeded. Retrying in " + (delay / 1000) + " seconds...", Toast.LENGTH_LONG).show();
                        new android.os.Handler().postDelayed(() -> fetchDealsWithRetry(call, retryCount + 1), delay);
                    } else if (response.code() == 429) {
                        Toast.makeText(requireContext(), "Rate limit exceeded. Please try again later.", Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("DEAL_FETCH", "❌ Unexpected response: " + response.code());
                        Toast.makeText(requireContext(), "❌ Failed to load deals", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DealResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("API", "Failed: " + t.getMessage());
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}