package com.example.functioninglogin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.HomePageUIClasses.GiftItem;
import com.example.functioninglogin.ShoppingListItem;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {

    private RecyclerView shoppingRecyclerView;
    private com.example.functioninglogin.ShoppingListAdapter adapter;
    private final List<ShoppingListItem> shoppingList = new ArrayList<>();

    public ShoppingListFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        shoppingRecyclerView = view.findViewById(R.id.shoppingRecyclerView);
        shoppingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new com.example.functioninglogin.ShoppingListAdapter(shoppingList);
        shoppingRecyclerView.setAdapter(adapter);

        fetchShoppingList();
        return view;
    }

    private void fetchShoppingList() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingList.clear();

                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    for (DataSnapshot memberSnap : listSnap.child("members").getChildren()) {
                        String memberName = memberSnap.child("name").getValue(String.class);

                        for (DataSnapshot giftSnap : memberSnap.child("gifts").getChildren()) {
                            GiftItem gift = giftSnap.getValue(GiftItem.class);
                            if (gift != null && gift.getName() != null) {
                                shoppingList.add(new ShoppingListItem(
                                        memberName,
                                        gift.getName(),
                                        gift.getPrice() != null ? gift.getPrice() : "0.00",
                                        gift.getStatus() != null ? gift.getStatus() : "idea"
                                ));
                            }
                        }
                    }
                }

                Log.d("SHOPPING_FETCH", "Total Items: " + shoppingList.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load shopping list: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("SHOPPING_ERROR", error.getMessage());
            }
        });
    }
}
