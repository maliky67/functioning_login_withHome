package com.example.functioninglogin.DiscountPage;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;

import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscountsFragment extends Fragment {

    private DealsAdapter adapter;
    private View loadingOverlay;
    private final List<DealItem> cachedDeals = new ArrayList<>();
    private long lastFetchTime = 0;
    private static final long FETCH_INTERVAL_MS = 5 * 60 * 1000;

    private ImageButton toggleFilterButton;
    private LinearLayout priceFilterContainer;
    private EditText minPriceEditText, maxPriceEditText;
    private Button applyFilterButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_discounts, container, false);

        ImageView helpButton = view.findViewById(R.id.help_button_discounts);
        helpButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("How to use discounts?")
                    .setMessage("Here you'll see updated offers that could help you save. You can filter by price and explore discounted gift ideas.")
                    .setPositiveButton("Ok!", null)
                    .show();
        });


        RecyclerView recyclerView = view.findViewById(R.id.dealsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DealsAdapter(deal -> {
            if (isAdded()) {
                DealDetailsFragment fragment = DealDetailsFragment.newInstance(deal);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        recyclerView.setAdapter(adapter);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // 🛠️ Filter UI setup
        toggleFilterButton = view.findViewById(R.id.toggleFilterButton);
        priceFilterContainer = view.findViewById(R.id.priceFilterContainer);
        minPriceEditText = view.findViewById(R.id.minPriceEditText);
        maxPriceEditText = view.findViewById(R.id.maxPriceEditText);
        applyFilterButton = view.findViewById(R.id.applyFilterButton);

        toggleFilterButton.setOnClickListener(v -> toggleFilterPanel());
        applyFilterButton.setOnClickListener(v -> applyPriceFilter());

        if (!cachedDeals.isEmpty()) {
            adapter.updateDeals(cachedDeals);
        }

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
        final long BASE_DELAY_MS = 60 * 1000;

        call.clone().enqueue(new Callback<DealResponse>() {
            @Override
            public void onResponse(@NonNull Call<DealResponse> call, @NonNull Response<DealResponse> response) {
                if (!isAdded()) return;

                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<DealItem> deals = response.body().getData().getDeals();
                    cachedDeals.clear();
                    cachedDeals.addAll(deals);
                    lastFetchTime = System.currentTimeMillis();
                    adapter.updateDeals(deals);
                } else {
                    if (response.code() == 429 && retryCount < MAX_RETRIES) {
                        long delay = BASE_DELAY_MS * (long) Math.pow(2, retryCount);
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Rate limit exceeded. Retrying in " + (delay / 1000) + " seconds...", Toast.LENGTH_LONG).show();
                        }
                        new Handler().postDelayed(() -> fetchDealsWithRetry(call, retryCount + 1), delay);
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
                if (!isAdded()) return;

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

    private void toggleFilterPanel() {
        if (priceFilterContainer.getVisibility() == View.GONE) {
            priceFilterContainer.setVisibility(View.VISIBLE);
            priceFilterContainer.setAlpha(0f);
            priceFilterContainer.animate().alpha(1f).setDuration(300).start();
        } else {
            priceFilterContainer.animate().alpha(0f).setDuration(300)
                    .withEndAction(() -> priceFilterContainer.setVisibility(View.GONE)).start();
        }
    }

    private void applyPriceFilter() {
        String minStr = minPriceEditText.getText().toString().trim();
        String maxStr = maxPriceEditText.getText().toString().trim();

        double min = minStr.isEmpty() ? 0.0 : Double.parseDouble(minStr);
        double max = maxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxStr);

        List<DealItem> filtered = new ArrayList<>();
        for (DealItem deal : cachedDeals) {
            if (deal.getDeal_price() != null && deal.getDeal_price().getAmount() != null) {
                try {
                    double price = Double.parseDouble(deal.getDeal_price().getAmount());
                    if (price >= min && price <= max) {
                        filtered.add(deal);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        adapter.updateDeals(filtered);
    }
}
