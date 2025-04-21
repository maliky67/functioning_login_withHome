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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
    private Spinner listSpinner;

    private boolean isPie = false;

    private final Map<String, List<GiftItem>> memberGifts = new HashMap<>();
    private final Map<String, List<BudgetData>> allListBudgets = new HashMap<>();
    private final Map<String, Double> listBudgets = new HashMap<>();

    private BudgetAdapter adapter;
    private ArrayAdapter<String> spinnerAdapter;
    private String selectedList = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        budgetRecyclerView = view.findViewById(R.id.budgetRecyclerView);
        chartToggleButton = view.findViewById(R.id.chartToggleButton);
        categoryTabs = view.findViewById(R.id.categoryTabs);
        titleTextView = view.findViewById(R.id.budgetTitleTextView);
        barChart = view.findViewById(R.id.budgetBarChart);
        pieChart = view.findViewById(R.id.budgetPieChart);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        progressOverlay = view.findViewById(R.id.progressOverlay);
        listSpinner = new Spinner(requireContext());
        ((LinearLayout) view.findViewById(R.id.budgetInnerLayout)).addView(listSpinner, 1); // insert below title

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
        progressOverlay.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allListBudgets.clear();
                listBudgets.clear();

                List<String> listTitles = new ArrayList<>();

                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    String listTitle = listSnap.child("listTitle").getValue(String.class);
                    if (listTitle == null) continue;
                    listTitles.add(listTitle);

                    double listBudget = 0.0;
                    try {
                        listBudget = listSnap.child("totalBudget").getValue(Double.class);
                    } catch (Exception ignored) {}

                    listBudgets.put(listTitle, listBudget);

                    List<BudgetData> members = new ArrayList<>();
                    for (DataSnapshot memberSnap : listSnap.child("members").getChildren()) {
                        String memberName = memberSnap.child("name").getValue(String.class);
                        String role = memberSnap.child("role").getValue(String.class);
                        String image = memberSnap.child("imageUrl").getValue(String.class);

                        List<GiftItem> gifts = new ArrayList<>();
                        for (DataSnapshot giftSnap : memberSnap.child("gifts").getChildren()) {
                            GiftItem gift = giftSnap.getValue(GiftItem.class);
                            if (gift != null) gifts.add(gift);
                        }

                        double total = 0;
                        for (GiftItem gift : gifts) {
                            try {
                                total += Double.parseDouble(gift.getPrice());
                            } catch (Exception ignored) {}
                        }

                        memberGifts.put(memberName, gifts);
                        members.add(new BudgetData(memberName, role, image, total, listBudget));
                    }

                    allListBudgets.put(listTitle, members);
                }

                setupSpinner(listTitles);
                progressOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load lists: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void setupSpinner(List<String> titles) {
        spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, titles);
        listSpinner.setAdapter(spinnerAdapter);

        if (!titles.isEmpty()) {
            selectedList = titles.get(0);
            listSpinner.setSelection(0);
            adapter.setBudgetList(allListBudgets.get(selectedList));
            updateChart();
        }

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedList = titles.get(position);
                adapter.setBudgetList(allListBudgets.get(selectedList));
                updateChart();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void updateChart() {
        if (!allListBudgets.containsKey(selectedList)) return;

        List<BudgetData> list = allListBudgets.get(selectedList);
        float maxY = 0;
        List<BarEntry> barEntries = new ArrayList<>();
        List<PieEntry> pieEntries = new ArrayList<>();

        int index = 0;
        for (BudgetData d : list) {
            float amount = (float) d.getTotalPrice();
            if (amount > 0) {
                barEntries.add(new BarEntry(index, amount));
                pieEntries.add(new PieEntry(amount, d.getMemberName()));
                maxY = Math.max(maxY, (float) d.getTotalBudget());
                index++;
            }
        }

        if (isPie) {
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "Gift Distribution");
            pieDataSet.setColors(getChartColors());
            pieChart.setData(new PieData(pieDataSet));
            pieChart.setDescription(getChartDescription("Pie Chart"));
            pieChart.invalidate();
        } else {
            BarDataSet barDataSet = new BarDataSet(barEntries, "Gift Spending");
            barDataSet.setColors(getChartColors());

            BarData data = new BarData(barDataSet);
            barChart.setData(data);

            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            YAxis yAxis = barChart.getAxisLeft();
            yAxis.setAxisMinimum(0f);
            yAxis.setAxisMaximum(maxY > 0 ? maxY : 100); // fallback

            barChart.getAxisRight().setEnabled(false);
            barChart.setDescription(getChartDescription("Bar Chart: Spending vs Budget"));
            barChart.invalidate();
        }

        emptyTextView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int[] getChartColors() {
        return new int[]{
                Color.BLUE, Color.MAGENTA, Color.GREEN, Color.CYAN, Color.RED,
                Color.YELLOW, Color.LTGRAY, Color.DKGRAY, Color.BLACK, Color.WHITE,
                Color.parseColor("#FFA500"), Color.parseColor("#FFC0CB"), Color.parseColor("#00CED1"),
                Color.parseColor("#FFD700"), Color.parseColor("#ADFF2F"), Color.parseColor("#8A2BE2"),
                Color.parseColor("#FF69B4"), Color.parseColor("#7FFF00"), Color.parseColor("#00FA9A"),
                Color.parseColor("#DC143C"), Color.parseColor("#FF8C00"), Color.parseColor("#1E90FF"),
                Color.parseColor("#9400D3"), Color.parseColor("#F4A460")
        };
    }

    private Description getChartDescription(String text) {
        Description description = new Description();
        description.setText(text);
        return description;
    }
}
