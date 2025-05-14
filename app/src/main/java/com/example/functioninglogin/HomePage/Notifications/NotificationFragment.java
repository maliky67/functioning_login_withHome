package com.example.functioninglogin.HomePage.Notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // ✅ CORRECT FRAGMENT
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.notificationRecyclerView);
        emptyText = view.findViewById(R.id.emptyNotificationsText);
        notificationList = new ArrayList<>(NotificationManager.getInstance().getNotifications());
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = getMockNotifications();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        toggleEmptyState();

        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    notificationList.remove(position);
                    adapter.notifyItemRemoved(position);
                    toggleEmptyState();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(" ");
        NotificationManager.getInstance().markAllRead(); // 🧹 Clear badge count
        requireActivity().invalidateOptionsMenu();
    }

    private List<NotificationItem> getMockNotifications() {
        List<NotificationItem> list = new ArrayList<>();
        list.add(new NotificationItem("🎉 Welcome!", "Thanks for installing the app!"));
        list.add(new NotificationItem("💸 Budget Alert", "You’ve hit 90% of your budget."));
        list.add(new NotificationItem("✅ Gift Bought!", "You marked \"Bluetooth Speaker\" as bought."));
        list.add(new NotificationItem("👤 New Member Added", "\"Uncle Mike\" was added to your Christmas list."));
        list.add(new NotificationItem("💸 Over Budget", "You've exceeded your list budget by $25. 😬"));
        list.add(new NotificationItem("⏰ 10 Days Left!", "Just 10 days until Christmas — time to wrap it up! 🎄"));
        list.add(new NotificationItem("🦖 Dinosaur Meat?!", "Just kidding... but it *is* on your wishlist. 😂"));

        return list;
    }

    private void toggleEmptyState() {
        if (notificationList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }
}
