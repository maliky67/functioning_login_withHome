package com.example.functioninglogin.HomePageUIClasses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {

    private TextView nameText;
    private ImageView memberImage;
    private Button saveButton;
    private ViewPager2 giftPager;

    private GiftPagerAdapter adapter;
    private List<GiftItem> giftItems = new ArrayList<>();

    private String memberKey, listKey, imageUrl;

    public static DetailFragment newInstance(String listKey, String memberKey, String name, String pref, String image) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("listKey", listKey);
        args.putString("memberKey", memberKey);
        args.putString("name", name);
        args.putString("pref", pref);
        args.putString("image", image);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        nameText = view.findViewById(R.id.detailTitle);
        memberImage = view.findViewById(R.id.detailImage);
        saveButton = view.findViewById(R.id.saveDetailsButton);
        giftPager = view.findViewById(R.id.giftPager);

        if (getArguments() != null) {
            listKey = getArguments().getString("listKey");
            memberKey = getArguments().getString("memberKey");
            imageUrl = getArguments().getString("image");
            nameText.setText(getArguments().getString("name"));

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext()).load(imageUrl).into(memberImage);
            } else {
                memberImage.setImageResource(R.drawable.baseline_account_box_24);
            }
        }

        loadGiftData();

        saveButton.setOnClickListener(v -> saveAllGiftItems());

        return view;
    }

    private void loadGiftData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference giftRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts");

        giftRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                giftItems.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    GiftItem item = snap.getValue(GiftItem.class);
                    if (item != null) {
                        item.setKey(snap.getKey());
                        giftItems.add(item);
                    }
                }

                // If no gifts, start with a blank one
                if (giftItems.isEmpty()) {
                    giftItems.add(new GiftItem());
                }

                adapter = new GiftPagerAdapter(giftItems, listKey, memberKey, () -> {
                    giftItems.add(new GiftItem());
                    adapter.notifyItemInserted(giftItems.size() - 1);
                    giftPager.setCurrentItem(giftItems.size() - 1, true);
                });

                giftPager.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error loading gifts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAllGiftItems() {
        if (adapter != null) {
            adapter.updateAllGiftItemsFromUI(); // 👈 Ensures EditTexts are synced with GiftItem list
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference baseRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts");

        boolean atLeastOneSaved = false;

        for (GiftItem item : giftItems) {
            if (item.getName() == null || item.getName().isEmpty()) continue;
            if (item.getPrice() == null || item.getPrice().isEmpty()) continue;

            if (item.getKey() == null) {
                String newKey = baseRef.push().getKey();
                item.setKey(newKey);
                baseRef.child(newKey).setValue(item);
            } else {
                baseRef.child(item.getKey()).setValue(item);
            }

            atLeastOneSaved = true;
        }

        if (atLeastOneSaved) {
            Toast.makeText(requireContext(), "Gifts saved to Firebase", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "No valid gift entries to save", Toast.LENGTH_SHORT).show();
        }
    }
}
