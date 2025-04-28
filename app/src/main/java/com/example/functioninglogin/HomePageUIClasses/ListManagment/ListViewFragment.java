package com.example.functioninglogin.HomePageUIClasses.ListManagment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberAdapter;
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberDataClass;
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberDataUploadFragment;
import com.example.functioninglogin.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ListViewFragment extends Fragment {

    private TextView emptyTextView;
    private List<MemberDataClass> memberList;
    private MemberAdapter memberAdapter;
    private DatabaseReference memberRef;
    private ValueEventListener memberListener;
    private String listKey;

    private TextView headerTitle, headerDesc, headerTotalSpent, headerTotalBudget;
    private ImageView headerImage;

    public static ListViewFragment newInstance(String key, String title, String budget, String imageUrl) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString("Key", key);
        args.putString("Title", title);
        args.putString("Budget", budget);
        args.putString("Image", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

        // View Binding
        CardView headerCard = view.findViewById(R.id.headerCard);
        headerTitle = view.findViewById(R.id.headerTitle);
        headerDesc = view.findViewById(R.id.headerDesc);
        headerImage = view.findViewById(R.id.headerImage);
        headerTotalSpent = view.findViewById(R.id.headerTotalSpent);
        headerTotalBudget = view.findViewById(R.id.headerTotalBudget);
        RecyclerView recyclerView = view.findViewById(R.id.ListrecyclerView);
        MaterialButton fab = view.findViewById(R.id.memberfab);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        memberList = new ArrayList<>();

        if (getArguments() != null) {
            listKey = getArguments().getString("Key", "");
            headerTitle.setText(getArguments().getString("Title", "List Name"));
            headerTotalBudget.setText("Total Budget: $" + getArguments().getString("Budget", "0"));
            String imageUrl = getArguments().getString("Image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext()).load(imageUrl).into(headerImage);
            } else {
                headerImage.setImageResource(R.drawable.baseline_account_box_24);
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        memberAdapter = new MemberAdapter(requireContext(), memberList, listKey);
        recyclerView.setAdapter(memberAdapter);

        headerCard.setOnClickListener(v -> {
            EditListFragment fragment = EditListFragment.newInstance(
                    listKey,
                    headerTitle.getText().toString(),
                    headerTotalBudget.getText().toString().replace("Total Budget: $", ""),
                    getArguments().getString("Image", "")
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members");

        memberListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberList.clear();
                double totalSpent = 0;
                int count = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    MemberDataClass member = snap.getValue(MemberDataClass.class);
                    if (member != null) {
                        member.setKey(snap.getKey());
                        Map<String, GiftItem> giftMap = new HashMap<>();
                        for (DataSnapshot giftSnap : snap.child("gifts").getChildren()) {
                            GiftItem gift = giftSnap.getValue(GiftItem.class);
                            if (gift != null) {
                                giftMap.put(giftSnap.getKey(), gift);

                                String status = gift.getStatus() != null ? gift.getStatus().toLowerCase() : "";
                                if (status.equals("bought") || status.equals("arrived") || status.equals("wrapped")) {
                                    try {
                                        totalSpent += Double.parseDouble(gift.getPrice());
                                    } catch (Exception ignored) {}
                                }
                            }
                        }

                        member.setGifts(giftMap);
                        memberList.add(member);
                        count++;
                    }
                }

                emptyTextView.setVisibility(memberList.isEmpty() ? View.VISIBLE : View.GONE);

                headerDesc.setText(count == 0 ? "No Members Yet" : (count == 1 ? "1 member" : count + " members"));
                headerTotalSpent.setText("Total Spent: $" + String.format("%.2f", totalSpent));

                // ✨ Overbudget detection ✨
                double budget = 0;
                try {
                    String budgetString = getArguments().getString("Budget", "0");
                    budget = Double.parseDouble(budgetString);
                } catch (Exception ignored) {}

                if (totalSpent > budget) {
                    // Overbudget: make background light red and text dark red
                    CardView headerCard = requireView().findViewById(R.id.headerCard);
                    headerCard.setCardBackgroundColor(requireContext().getResources().getColor(R.color.overbudget_background));
                    headerTotalSpent.setTextColor(requireContext().getResources().getColor(android.R.color.holo_red_dark));
                    headerTitle.setTextColor(requireContext().getResources().getColor(R.color.christmas_blue));
                    headerDesc.setTextColor(requireContext().getResources().getColor(R.color.christmas_blue));
                    headerTotalBudget.setTextColor(requireContext().getResources().getColor(R.color.christmas_blue));
                } else {
                    // Normal colors
                    CardView headerCard = requireView().findViewById(R.id.headerCard);
                    headerCard.setCardBackgroundColor(requireContext().getResources().getColor(R.color.white)); // or your default
                }

                memberAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load members: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        memberRef.addValueEventListener(memberListener);

        fab.setOnClickListener(v -> {
            MemberDataUploadFragment fragment = MemberDataUploadFragment.newInstance(listKey);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                MemberDataClass toDelete = memberList.get(position);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Member?")
                        .setMessage("Delete \"" + toDelete.getName() + "\" and all their gifts?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteMemberFromFirebase(toDelete.getKey());
                            // Do not manually remove or notify — listener will auto-refresh
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Restore the swiped item visually
                            memberAdapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(dialog -> {
                            // If dialog is dismissed via back button, also restore
                            memberAdapter.notifyItemChanged(position);
                        })
                        .show();
            }

        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        getParentFragmentManager().setFragmentResultListener("refreshList", this, (requestKey, bundle) -> {
            if (bundle.getBoolean("refreshListHeader", false)) {
                DatabaseReference listMetaRef = FirebaseDatabase.getInstance()
                        .getReference("Unique User ID")
                        .child(userId)
                        .child("lists")
                        .child(listKey);

                listMetaRef.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String updatedTitle = snapshot.child("listTitle").getValue(String.class);
                        String updatedImage = snapshot.child("listImage").getValue(String.class);
                        Double updatedBudget = snapshot.child("totalBudget").getValue(Double.class);

                        headerTitle.setText(updatedTitle != null ? updatedTitle : "List Name");
                        headerTotalBudget.setText("Total Budget: $" + (updatedBudget != null ? updatedBudget : 0));

                        if (updatedImage != null && !updatedImage.isEmpty()) {
                            Glide.with(requireContext()).load(updatedImage).into(headerImage);
                        } else {
                            headerImage.setImageResource(R.drawable.baseline_account_box_24);
                        }
                        // 🛎️ Re-check if overbudget immediately after fetching updated budget
                        double totalSpent = 0;
                        try {
                            String spentStr = headerTotalSpent.getText().toString().replace("Total Spent: $", "");
                            totalSpent = Double.parseDouble(spentStr);
                        } catch (Exception ignored) {}

                        if (updatedBudget != null && totalSpent > updatedBudget) {
                            headerCard.setCardBackgroundColor(requireContext().getResources().getColor(R.color.overbudget_background));
                            headerTotalSpent.setTextColor(requireContext().getResources().getColor(android.R.color.holo_red_dark));
                            headerTitle.setTextColor(requireContext().getResources().getColor(R.color.christmas_blue));
                            headerDesc.setTextColor(requireContext().getResources().getColor(R.color.christmas_blue));
                            headerTotalBudget.setTextColor(requireContext().getResources().getColor(R.color.christmas_blue));
                        } else {
                            headerCard.setCardBackgroundColor(requireContext().getResources().getColor(R.color.white));
                            headerTitle.setTextColor(requireContext().getResources().getColor(R.color.BorderBlue));
                            headerDesc.setTextColor(requireContext().getResources().getColor(R.color.BorderBlue));
                            headerTotalBudget.setTextColor(requireContext().getResources().getColor(R.color.BorderBlue));
                            headerTotalSpent.setTextColor(requireContext().getResources().getColor(R.color.BorderBlue));
                            ;
                        }
                    }
                });
            }
        });

        return view;
    }

    private void deleteMemberFromFirebase(String memberKey) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(requireContext(), "Member deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error deleting member", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (memberRef != null && memberListener != null) {
            memberRef.removeEventListener(memberListener);
        }
    }
}
