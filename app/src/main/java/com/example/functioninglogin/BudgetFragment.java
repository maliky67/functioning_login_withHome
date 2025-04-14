package com.example.functioninglogin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.BudgetPageUIClasses.BudgetAdapter;
import com.example.functioninglogin.BudgetPageUIClasses.BudgetData;
import com.example.functioninglogin.HomePageUIClasses.GiftItem;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {

    private RecyclerView budgetRecyclerView;
    private ImageButton chartToggleButton;
    private RadioGroup categoryTabs;
    private TextView titleTextView, subtitleTextView;
    private BarChart barChart;
    private PieChart pieChart;

    private boolean isPie = false;
    private final Map<String, List<GiftItem>> memberGifts = new HashMap<>();
    private BudgetAdapter adapter;

    // Remove this entire onCreate block
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // listKey is no longer needed
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        budgetRecyclerView = view.findViewById(R.id.budgetRecyclerView);
        chartToggleButton = view.findViewById(R.id.chartToggleButton);
        categoryTabs = view.findViewById(R.id.categoryTabs);
        titleTextView = view.findViewById(R.id.budgetTitleTextView);
        subtitleTextView = view.findViewById(R.id.budgetSubtitleTextView);
        barChart = view.findViewById(R.id.budgetBarChart);
        pieChart = view.findViewById(R.id.budgetPieChart);

        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter(new ArrayList<>());
        budgetRecyclerView.setAdapter(adapter);

        chartToggleButton.setOnClickListener(v -> toggleChart());
        categoryTabs.setOnCheckedChangeListener((group, checkedId) -> updateChart());

        loadBudgetData();
        return view;
    }

    private void toggleChart() {
        isPie = !isPie;
        pieChart.setVisibility(isPie ? View.VISIBLE : View.GONE);
        barChart.setVisibility(isPie ? View.GONE : View.VISIBLE);
        updateChart();
    }

    private void loadBudgetData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BudgetData> budgetDataList = new ArrayList<>();
                memberGifts.clear();

                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    String listTitle = listSnap.child("listTitle").getValue(String.class);
                    DataSnapshot membersSnap = listSnap.child("members");

                    for (DataSnapshot memberSnap : membersSnap.getChildren()) {
                        String memberName = memberSnap.child("name").getValue(String.class);
                        String role = memberSnap.child("role").getValue(String.class);
                        String image = memberSnap.child("imageUrl").getValue(String.class);

                        List<GiftItem> gifts = new ArrayList<>();
                        for (DataSnapshot giftSnap : memberSnap.child("gifts").getChildren()) {
                            GiftItem gift = giftSnap.getValue(GiftItem.class);
                            if (gift != null) {
                                gifts.add(gift);
                            }
                        }

                        memberGifts.put(memberName, gifts);
                        double total = 0;
                        for (GiftItem gift : gifts) {
                            try {
                                total += Double.parseDouble(gift.getPrice());
                            } catch (Exception ignored) {}
                        }

                        budgetDataList.add(new BudgetData(memberName, role, image, total));
                    }
                }

                adapter.setBudgetList(budgetDataList);
                updateChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load lists: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateChart() {
        List<BarEntry> barEntries = new ArrayList<>();
        List<PieEntry> pieEntries = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, List<GiftItem>> entry : memberGifts.entrySet()) {
            String name = entry.getKey();
            List<GiftItem> gifts = entry.getValue();

            double total = 0;
            for (GiftItem gift : gifts) {
                boolean matchesFilter = true;
                int selectedId = categoryTabs.getCheckedRadioButtonId();
                String status = gift.getStatus() != null ? gift.getStatus().toLowerCase() : "idea";

                if (selectedId == R.id.tabIdeas && !status.equals("idea")) matchesFilter = false;
                if (selectedId == R.id.tabPurchased && !status.equals("bought")) matchesFilter = false;
                if (selectedId == R.id.tabGifts) {
                    total = gifts.size();
                    break;
                }

                if (matchesFilter) {
                    try {
                        total += Double.parseDouble(gift.getPrice());
                    } catch (Exception ignored) {}
                }
            }

            if (total > 0) {
                barEntries.add(new BarEntry(index, (float) total));
                pieEntries.add(new PieEntry((float) total, name));
                index++;
            }
        }

        if (isPie) {
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "Budget Breakdown");
            pieDataSet.setColors(new int[]{Color.BLUE, Color.MAGENTA, Color.GREEN, Color.CYAN});
            pieChart.setData(new PieData(pieDataSet));
            pieChart.setDescription(getChartDescription("Pie Chart View"));
            pieChart.invalidate();
        } else {
            BarDataSet barDataSet = new BarDataSet(barEntries, "Budget Total");
            BarData barData = new BarData(barDataSet);
            barChart.setData(barData);
            barChart.setDescription(getChartDescription("Bar Chart View"));
            barChart.invalidate();
        }
    }

    private Description getChartDescription(String text) {
        Description description = new Description();
        description.setText(text);
        return description;
    }
}
