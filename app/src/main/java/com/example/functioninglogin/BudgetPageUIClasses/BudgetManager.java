package com.example.functioninglogin.BudgetPageUIClasses;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.functioninglogin.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class BudgetManager {

    private static final String CHANNEL_ID = "budget_over_notification";
    private static final int NOTIFICATION_ID = 1;

    public static class BudgetStatus {
        public double totalSpent;
        public double budget;
        public boolean isOverBudget;
        public String overBudgetGiftName;
        public String overBudgetMemberName;
        public double overBudgetGiftPrice;

        public BudgetStatus(double totalSpent, double budget, boolean isOverBudget,
                            String overBudgetGiftName, String overBudgetMemberName, double overBudgetGiftPrice) {
            this.totalSpent = totalSpent;
            this.budget = budget;
            this.isOverBudget = isOverBudget;
            this.overBudgetGiftName = overBudgetGiftName;
            this.overBudgetMemberName = overBudgetMemberName;
            this.overBudgetGiftPrice = overBudgetGiftPrice;
        }
    }

    public interface BudgetCheckCallback {
        void onBudgetChecked(BudgetStatus status);
    }

    public static void checkBudget(Context context, String userId, String listId, BudgetCheckCallback callback) {
        DatabaseReference listRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listId);

        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double budget = 0;
                double totalSpent = 0;
                String overBudgetGiftName = null;
                String overBudgetMemberName = null;
                double overBudgetGiftPrice = 0;

                // Get the budget
                String budgetStr = snapshot.child("budget").getValue(String.class);
                if (budgetStr != null) {
                    try {
                        budget = Double.parseDouble(budgetStr);
                    } catch (NumberFormatException e) {
                        Log.e("BudgetManager", "Invalid budget format: " + budgetStr);
                    }
                }

                // Calculate total spent on purchased gifts
                DataSnapshot membersSnap = snapshot.child("members");
                for (DataSnapshot memberSnap : membersSnap.getChildren()) {
                    String memberName = memberSnap.child("name").getValue(String.class);
                    DataSnapshot giftsSnap = memberSnap.child("gifts");
                    for (DataSnapshot giftSnap : giftsSnap.getChildren()) {
                        String status = giftSnap.child("status").getValue(String.class);
                        if ("Purchased".equals(status)) {
                            String priceStr = giftSnap.child("price").getValue(String.class);
                            double price = 0;
                            if (priceStr != null) {
                                try {
                                    price = Double.parseDouble(priceStr);
                                } catch (NumberFormatException e) {
                                    Log.e("BudgetManager", "Invalid price format: " + priceStr);
                                }
                            }
                            double previousTotal = totalSpent;
                            totalSpent += price;
                            // Check if this gift causes the overbudget
                            if (previousTotal <= budget && totalSpent > budget) {
                                overBudgetGiftName = giftSnap.child("name").getValue(String.class);
                                overBudgetMemberName = memberName;
                                overBudgetGiftPrice = price;
                            }
                        }
                    }
                }

                boolean isOverBudget = totalSpent > budget;
                BudgetStatus status = new BudgetStatus(totalSpent, budget, isOverBudget,
                        overBudgetGiftName, overBudgetMemberName, overBudgetGiftPrice);

                // Trigger notification if overbudget
                if (isOverBudget && overBudgetGiftName != null && overBudgetMemberName != null) {
                    sendOverBudgetNotification(context, listId, snapshot.child("listTitle").getValue(String.class),
                            overBudgetMemberName, overBudgetGiftName, overBudgetGiftPrice, totalSpent, budget);
                }

                callback.onBudgetChecked(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("BudgetManager", "Failed to check budget: " + error.getMessage());
                callback.onBudgetChecked(new BudgetStatus(0, 0, false, null, null, 0));
            }
        });
    }

    private static void sendOverBudgetNotification(Context context, String listId, String listTitle,
                                                   String memberName, String giftName, double giftPrice,
                                                   double totalSpent, double budget) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Budget Overrun Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifies when a list exceeds its budget");
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        String message = String.format("List '%s' is over budget! %s's gift '%s' ($%.2f) caused the overrun. Total spent: $%.2f, Budget: $%.2f",
                listTitle, memberName, giftName, giftPrice, totalSpent, budget);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_clear_24) // Use an appropriate icon
                .setContentTitle("Budget Exceeded")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(listId.hashCode() + NOTIFICATION_ID, builder.build());
    }
}