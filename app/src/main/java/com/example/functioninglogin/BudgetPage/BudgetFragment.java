package com.example.functioninglogin.BudgetPage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.HomePage.GiftManagment.GiftItem;
import com.example.functioninglogin.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class BudgetFragment extends Fragment {

    private RadioGroup categoryTabs;
    private TextView emptyTextView;
    private BarChart barChart;
    private PieChart pieChart;
    private FrameLayout progressOverlay;
    private Spinner listSpinner;

    private boolean isPie = true;

    private final Map<String, List<BudgetData>> allListBudgets = new HashMap<>();
    private final Map<String, Double> listBudgets = new HashMap<>();
    private BudgetAdapter adapter;
    private String selectedList = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        RecyclerView budgetRecyclerView = view.findViewById(R.id.budgetRecyclerView);
        ImageButton chartToggleButton = view.findViewById(R.id.chartToggleButton);
        categoryTabs = view.findViewById(R.id.categoryTabs);
        barChart = view.findViewById(R.id.budgetBarChart);
        pieChart = view.findViewById(R.id.budgetPieChart);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        progressOverlay = view.findViewById(R.id.progressOverlay);

        listSpinner = new Spinner(requireContext());
        ((LinearLayout) view.findViewById(R.id.budgetInnerLayout)).addView(listSpinner, 1);

        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter(new ArrayList<>());
        budgetRecyclerView.setAdapter(adapter);

        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);
        isPie = true;

        chartToggleButton.setOnClickListener(v -> toggleChart());
        categoryTabs.setOnCheckedChangeListener((group, checkedId) -> updateChart());

        disableChartInteractivity(barChart);
        loadBudgetData();
        updateChart();
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
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
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
                            if (gift != null) {
                                gifts.add(gift);
                            }
                        }

                        BudgetData member = new BudgetData(memberName, role, image, 0, listBudget);
                        member.setGifts(gifts);
                        members.add(member);
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

    private boolean shouldIncludeGift(GiftItem gift) {
        int selectedId = categoryTabs.getCheckedRadioButtonId();
        String status = gift.getStatus() != null ? gift.getStatus().toLowerCase() : "";

        if (selectedId == R.id.tabIdeas) {
            return status.equals("idea");
        } else if (selectedId == R.id.tabPurchased) {
            return status.equals("bought") || status.equals("wrapped") || status.equals("arrived");
        } else {
            return true;
        }
    }

    private void setupSpinner(List<String> titles) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, titles);
        listSpinner.setAdapter(spinnerAdapter);

        if (!titles.isEmpty()) {
            selectedList = titles.get(0);
            listSpinner.setSelection(0);
            updateChart();
        }

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedList = titles.get(position);
                updateChart();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateChart() {
        if (!allListBudgets.containsKey(selectedList)) return;

        double listBudget = listBudgets.getOrDefault(selectedList, 0.0);
        List<BudgetData> allMembers = allListBudgets.get(selectedList);

        List<BudgetData> filteredMembers = new ArrayList<>();
        List<BarEntry> stackedEntries = new ArrayList<>();
        List<Float> stack = new ArrayList<>();
        List<PieEntry> pieEntries = new ArrayList<>();
        List<String> memberNames = new ArrayList<>();
        float totalSpent = 0f;

        int[] rawColors = getChartColors();
        int colorIndex = 0;

        for (BudgetData member : Objects.requireNonNull(allMembers)) {
            float value = 0f;
            List<GiftItem> gifts = member.getGifts();
            if (gifts != null) {
                for (GiftItem gift : gifts) {
                    if (shouldIncludeGift(gift)) {
                        try {
                            value += Float.parseFloat(gift.getPrice());
                        } catch (Exception ignored) {}
                    }
                }
            }

            if (value > 0f) {
                BudgetData filtered = new BudgetData(
                        member.getMemberName(),
                        member.getMemberRole(),
                        member.getMemberImageUrl(),
                        value,
                        listBudget
                );
                filtered.setGifts(gifts);
                filtered.setAssignedColor(rawColors[colorIndex % rawColors.length]);
                colorIndex++;

                filteredMembers.add(filtered);
                stack.add(value);
                memberNames.add(member.getMemberName());
                pieEntries.add(new PieEntry(value, member.getMemberName()));
                totalSpent += value;
            }
        }

        float remaining = (float) listBudget - totalSpent;
        boolean isOverBudget = totalSpent > listBudget;

        if (isOverBudget) {
            float overAmount = totalSpent - (float) listBudget;
            sendOverBudgetNotification(selectedList, overAmount);
        }

        if (remaining > 0) {
            stack.add(remaining);
            memberNames.add("Remaining");
            pieEntries.add(new PieEntry(remaining, "Remaining"));
        }

        if (isPie) {
            PieData pieData = getPieData(pieEntries, remaining, rawColors);
            pieChart.setData(pieData);
            pieChart.setDescription(getChartDescription(isOverBudget ? "Over Budget!" : "Pie Chart"));
            pieChart.setHoleColor(isOverBudget ? Color.parseColor("#FFCCCC") : Color.TRANSPARENT);

            if (isOverBudget) {
                float overAmount = totalSpent - (float) listBudget;
                pieChart.setCenterText("⚠️ Over Budget!\n($" + String.format("%.2f", overAmount) + ")");
            } else {
                pieChart.setCenterText("");
            }

            pieChart.setCenterTextSize(18f);
            pieChart.setCenterTextColor(isOverBudget ? Color.RED : Color.BLACK);
            pieChart.invalidate();
        } else {
            if (!stack.isEmpty()) {
                float[] barStack = new float[stack.size()];
                for (int i = 0; i < stack.size(); i++) barStack[i] = stack.get(i);

                BarEntry entry = new BarEntry(0, barStack);
                stackedEntries.add(entry);

                BarDataSet barDataSet = new BarDataSet(stackedEntries, "Spending vs Budget");

                List<Integer> barColors = new ArrayList<>();
                for (int i = 0; i < stack.size(); i++) {
                    if (i == stack.size() - 1 && remaining > 0) {
                        barColors.add(isOverBudget ? Color.RED : Color.WHITE);
                    } else {
                        barColors.add(rawColors[i % rawColors.length]);
                    }
                }

                barDataSet.setColors(barColors);
                barDataSet.setStackLabels(memberNames.toArray(new String[0]));
                barDataSet.setDrawValues(true);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(14f);

                barDataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getBarStackedLabel(float value, BarEntry entry) {
                        if (value <= 0f) return "";
                        return "$" + String.format("%.2f", value);
                    }
                });

                BarData barData = new BarData(barDataSet);
                barData.setBarWidth(0.4f);
                barChart.setData(barData);

                XAxis xAxis = barChart.getXAxis();
                xAxis.setEnabled(false);

                YAxis yAxis = barChart.getAxisLeft();
                yAxis.setAxisMinimum(0f);
                yAxis.setAxisMaximum(isOverBudget ? totalSpent * 1.1f : (float) listBudget);
                yAxis.setGranularity(10f);

                yAxis.removeAllLimitLines();
                if (listBudget > 0) {
                    LimitLine budgetLine = new LimitLine((float) listBudget, "Budget");
                    budgetLine.setLineColor(Color.RED);
                    budgetLine.setLineWidth(2f);
                    budgetLine.setTextColor(Color.RED);
                    budgetLine.setTextSize(12f);
                    yAxis.addLimitLine(budgetLine);
                }

                barChart.getAxisRight().setEnabled(false);
                barChart.setDescription(getChartDescription(""));
                barChart.invalidate();
            }
        }

        adapter.setBudgetList(filteredMembers);
        emptyTextView.setVisibility(filteredMembers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void sendOverBudgetNotification(String listName, double overAmount) {
        String channelId = "over_budget_channel";
        String channelName = "Over Budget Alerts";

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications when a list goes over budget");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.baseline_access_alarms_24)
                .setContentTitle("⚠️ Budget Alert!")
                .setContentText(listName + " list is over budget by $" + String.format("%.2f", overAmount) + "!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(listName.hashCode(), builder.build());
        }
    }

    @NonNull
    private static PieData getPieData(List<PieEntry> pieEntries, float remaining, int[] rawColors) {
        List<Integer> pieColors = new ArrayList<>();
        for (int i = 0; i < pieEntries.size(); i++) {
            if (i == pieEntries.size() - 1 && remaining > 0) {
                pieColors.add(Color.WHITE);
            } else {
                pieColors.add(rawColors[i % rawColors.length]);
            }
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, " ");
        pieDataSet.setColors(pieColors);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(14f);

        pieDataSet.setValueLineColor(Color.BLACK);
        pieDataSet.setValueLinePart1Length(0.3f);
        pieDataSet.setValueLinePart2Length(0.4f);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        pieDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return "$" + String.format("%.2f", value);
            }
        });

        return new PieData(pieDataSet);
    }

    private int[] getChartColors() {
        return new int[]{
                Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA,
                Color.YELLOW, Color.LTGRAY, Color.DKGRAY, Color.BLACK,
                Color.parseColor("#FFA500"), Color.parseColor("#FFC0CB"), Color.parseColor("#00CED1"),
                Color.parseColor("#FFD700"), Color.parseColor("#ADFF2F"), Color.parseColor("#8A2BE2"),
                Color.parseColor("#FF69B4"), Color.parseColor("#7FFF00"), Color.parseColor("#00FA9A"),
                Color.parseColor("#DC143C"), Color.parseColor("#FF8C00"), Color.parseColor("#1E90FF"),
                Color.parseColor("#9400D3"), Color.parseColor("#F4A460")
        };
    }

    private void disableChartInteractivity(BarLineChartBase<?> chart) {
        chart.setTouchEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setClickable(false);
    }

    private Description getChartDescription(String text) {
        Description description = new Description();
        description.setText(text);
        return description;
    }
}
