package com.example.functioninglogin.BudgetPageUIClasses;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class BudgetFragment extends Fragment {

    private RecyclerView budgetRecyclerView;
    private ImageButton chartToggleButton;
    private RadioGroup categoryTabs;
    private TextView titleTextView, emptyTextView;
    private BarChart barChart;
    private PieChart pieChart;
    private FrameLayout progressOverlay;

    private boolean isPie = false;
    private final Map<String, List<GiftItem>> memberGifts = new HashMap<>();
    private BudgetAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        budgetRecyclerView = view.findViewById(R.id.budgetRecyclerView);
        chartToggleButton = view.findViewById(R.id.chartToggleButton);
        categoryTabs = view.findViewById(R.id.categoryTabs);
        titleTextView = view.findViewById(R.id.budgetTitleTextView);
        barChart = view.findViewById(R.id.budgetBarChart);
        pieChart = view.findViewById(R.id.budgetPieChart);
        emptyTextView = view.findViewById(R.id.emptyTextView); // Add this TextView in XML
        progressOverlay = view.findViewById(R.id.progressOverlay); // Add FrameLayout to XML

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
        progressOverlay.setVisibility(View.VISIBLE); // ✅ Show loading

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

                // ✅ Hide progress after full chart update
                progressOverlay.setVisibility(View.GONE);

                // ✅ Show empty state if needed
                emptyTextView.setVisibility(budgetDataList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load lists: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressOverlay.setVisibility(View.GONE); // ✅ Hide progress on failure too
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
            pieDataSet.setColors(new int[]{
                    Color.BLUE, Color.MAGENTA, Color.GREEN, Color.CYAN,
                    Color.RED, Color.YELLOW, Color.LTGRAY, Color.DKGRAY,
                    Color.BLACK, Color.WHITE, Color.parseColor("#FFA500"), // Orange
                    Color.parseColor("#FFC0CB"), // Pink
                    Color.parseColor("#00CED1"), // Dark Turquoise
                    Color.parseColor("#FFD700"), // Gold
                    Color.parseColor("#ADFF2F"), // Green Yellow
                    Color.parseColor("#8A2BE2"), // Blue Violet
                    Color.parseColor("#FF69B4"), // Hot Pink
                    Color.parseColor("#7FFF00"), // Chartreuse
                    Color.parseColor("#00FA9A"), // Medium Spring Green
                    Color.parseColor("#DC143C"), // Crimson
                    Color.parseColor("#FF8C00"), // Dark Orange
                    Color.parseColor("#1E90FF"), // Dodger Blue
                    Color.parseColor("#9400D3"), // Dark Violet
                    Color.parseColor("#F4A460"), // Sandy Brown
            });
            pieChart.setData(new PieData(pieDataSet));
            pieChart.setDescription(getChartDescription("Pie Chart View"));
            pieChart.invalidate();
        } else {
            BarDataSet barDataSet = new BarDataSet(barEntries, "Budget Total");
            barDataSet.setColors(new int[]{
                    Color.BLUE, Color.MAGENTA, Color.GREEN, Color.CYAN,
                    Color.RED, Color.YELLOW, Color.LTGRAY, Color.DKGRAY,
                    Color.BLACK, Color.WHITE, Color.parseColor("#FFA500"), // Orange
                    Color.parseColor("#FFC0CB"), // Pink
                    Color.parseColor("#00CED1"), // Dark Turquoise
                    Color.parseColor("#FFD700"), // Gold
                    Color.parseColor("#ADFF2F"), // Green Yellow
                    Color.parseColor("#8A2BE2"), // Blue Violet
                    Color.parseColor("#FF69B4"), // Hot Pink
                    Color.parseColor("#7FFF00"), // Chartreuse
                    Color.parseColor("#00FA9A"), // Medium Spring Green
                    Color.parseColor("#DC143C"), // Crimson
                    Color.parseColor("#FF8C00"), // Dark Orange
                    Color.parseColor("#1E90FF"), // Dodger Blue
                    Color.parseColor("#9400D3"), // Dark Violet
                    Color.parseColor("#F4A460"), // Sandy Brown
            });
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
