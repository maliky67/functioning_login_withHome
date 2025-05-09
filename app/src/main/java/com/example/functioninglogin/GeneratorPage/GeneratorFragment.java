package com.example.functioninglogin.GeneratorPage;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;


import com.example.functioninglogin.HomePage.GiftManagment.GiftItem;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class GeneratorFragment extends Fragment {

    private ShoppingListAdapter adapter;
    private final List<ShoppingListItem> shoppingList = new ArrayList<>();
    private FrameLayout progressOverlay;
    private TextView emptyListText;
    private RecyclerView shoppingRecyclerView;
    private ValueEventListener shoppingListListener;

    public GeneratorFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_generator, container, false);

        ImageView helpButton = view.findViewById(R.id.help_button_generator);
        helpButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("What does the generator do?")
                    .setMessage("This screen generates gift ideas based on interests and budget. You can customize options and add suggested gifts to your registry.")
                    .setPositiveButton("Ok!", null)
                    .show();
        });


        shoppingRecyclerView = view.findViewById(R.id.shoppingRecyclerView);
        emptyListText = view.findViewById(R.id.emptyListText);
        progressOverlay = view.findViewById(R.id.progressOverlay);

        shoppingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShoppingListAdapter(shoppingList);
        shoppingRecyclerView.setAdapter(adapter);

        fetchShoppingList();
        return view;
    }

    private void showLoading() {
        if (progressOverlay != null) progressOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
    }

    private void fetchShoppingList() {
        showLoading();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists");

        if (shoppingListListener != null) {
            dbRef.removeEventListener(shoppingListListener);
        }

        shoppingListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingList.clear();

                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    String listId = listSnap.getKey();

                    for (DataSnapshot memberSnap : listSnap.child("members").getChildren()) {
                        String memberId = memberSnap.getKey();
                        String memberName = memberSnap.child("name").getValue(String.class);

                        for (DataSnapshot giftSnap : memberSnap.child("gifts").getChildren()) {
                            GiftItem gift = giftSnap.getValue(GiftItem.class);
                            if (gift != null && gift.getName() != null) {
                                shoppingList.add(new ShoppingListItem(
                                        listId,
                                        memberId,
                                        giftSnap.getKey(),
                                        memberName,
                                        gift.getName(),
                                        gift.getPrice() != null ? gift.getPrice() : "0.00",
                                        gift.getStatus() != null ? gift.getStatus() : "idea"
                                ));
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                updateEmptyStateVisibility();
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "❌ Failed to load shopping list: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("SHOPPING_ERROR", error.getMessage());
                hideLoading();
            }
        };

        dbRef.addValueEventListener(shoppingListListener);
    }

    private void updateGiftStatus(ShoppingListItem item, boolean isChecked) {
        String newStatus = isChecked ? "bought" : "idea";

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference giftRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(item.getListId())
                .child("members")
                .child(item.getMemberId())
                .child("gifts")
                .child(item.getGiftId())
                .child("status");

        giftRef.setValue(newStatus)
                .addOnSuccessListener(aVoid ->
                        Log.d("STATUS_UPDATE", "✅ Updated status for " + item.getGiftName() + " to " + newStatus)
                )
                .addOnFailureListener(e ->
                        Log.e("STATUS_UPDATE", "❌ Failed to update status", e)
                );
    }

    private void updateEmptyStateVisibility() {
        if (shoppingList.isEmpty()) {
            emptyListText.setVisibility(View.VISIBLE);
            shoppingRecyclerView.setVisibility(View.GONE);
        } else {
            emptyListText.setVisibility(View.GONE);
            shoppingRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists");
        if (shoppingListListener != null) {
            dbRef.removeEventListener(shoppingListListener);
        }
    }
}
