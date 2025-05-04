package com.example.functioninglogin.DiscountPageUIClasses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DealDetailsFragment extends Fragment {

    private DealItem deal;
    private Spinner listSpinner, memberSpinner;

    private List<String> listNames = new ArrayList<>();
    private List<String> listIds = new ArrayList<>();
    private List<String> memberNames = new ArrayList<>();
    private List<String> memberIds = new ArrayList<>();
    private Map<String, List<String>> listMembersMap = new HashMap<>(); // Maps listId to memberIds

    public static DealDetailsFragment newInstance(DealItem deal) {
        DealDetailsFragment fragment = new DealDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("deal", deal); // ✅ Correct way for Parcelable
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deal_details, container, false);

        // Retrieve the deal from arguments
        if (getArguments() != null) {
            deal =  getArguments().getParcelable("deal");
        }

        // Initialize views
        // Added ImageView field
        ImageView dealImageView = view.findViewById(R.id.dealImageView); // Initialize the ImageView
        TextView dealTitleTextView = view.findViewById(R.id.dealNameTextView);
        TextView dealPriceTextView = view.findViewById(R.id.dealPriceTextView);
        TextView dealUrlTextView = view.findViewById(R.id.dealUrlTextView);
        TextView dealDescriptionTextView = view.findViewById(R.id.dealDescriptionTextView);
        listSpinner = view.findViewById(R.id.listSpinner);
        memberSpinner = view.findViewById(R.id.memberSpinner);
        Button addGiftButton = view.findViewById(R.id.addGiftButton);

        // Populate deal details with null checks
        if (deal != null) {
            // Load the image
            if (dealImageView != null) {
                String imageUrl = deal.getDeal_photo();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.baseline_attach_money_24)
                            .error(R.drawable.baseline_clear_24)
                            .into(dealImageView);
                } else {
                    dealImageView.setImageResource(R.drawable.baseline_lightbulb_24);
                }
            } else {
                Toast.makeText(getContext(), "dealImageView is null", Toast.LENGTH_LONG).show();
            }

            if (dealTitleTextView != null) {
                dealTitleTextView.setText(deal.getDeal_title() != null ? deal.getDeal_title() : "No title");
            } else {
                Toast.makeText(getContext(), "dealNameTextView is null", Toast.LENGTH_LONG).show();
            }

            String price = (deal.getDeal_price() != null && deal.getDeal_price().getAmount() != null)
                    ? "$" + deal.getDeal_price().getAmount()
                    : "Price N/A";
            if (dealPriceTextView != null) {
                dealPriceTextView.setText(price);
            } else {
                Toast.makeText(getContext(), "dealPriceTextView is null", Toast.LENGTH_LONG).show();
            }

            if (dealUrlTextView != null) {
                dealUrlTextView.setText(deal.getDeal_url() != null ? deal.getDeal_url() : "No link");
            } else {
                Toast.makeText(getContext(), "dealUrlTextView is null", Toast.LENGTH_LONG).show();
            }

            if (dealDescriptionTextView != null) {
                dealDescriptionTextView.setText(deal.getDescription());
            } else {
                Toast.makeText(getContext(), "dealDescriptionTextView is null", Toast.LENGTH_LONG).show();
            }
        }

        // Load lists and members from Firebase
        loadListsAndMembers();

        if (addGiftButton != null) {
            addGiftButton.setOnClickListener(v -> addGiftToMember());
        } else {
            Toast.makeText(getContext(), "addGiftButton is null", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void loadListsAndMembers() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference listsRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists");

        listsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listNames.clear();
                listIds.clear();
                listMembersMap.clear();

                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    String listId = listSnap.getKey();
                    String listTitle = listSnap.child("listTitle").getValue(String.class);

                    if (listId != null && listTitle != null) {
                        listNames.add(listTitle);
                        listIds.add(listId);

                        // Load members for this list
                        List<String> membersInList = new ArrayList<>();
                        DataSnapshot membersSnap = listSnap.child("members");
                        for (DataSnapshot memberSnap : membersSnap.getChildren()) {
                            String memberId = memberSnap.getKey();
                            String memberName = memberSnap.child("name").getValue(String.class);
                            if (memberId != null && memberName != null) {
                                membersInList.add(memberId);
                                if (!memberNames.contains(memberName)) {
                                    memberNames.add(memberName);
                                    memberIds.add(memberId);
                                }
                            }
                        }
                        listMembersMap.put(listId, membersInList);
                    }
                }

                // Populate the list spinner
                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, listNames);
                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if (listSpinner != null) {
                    listSpinner.setAdapter(listAdapter);
                } else {
                    Toast.makeText(getContext(), "listSpinner is null", Toast.LENGTH_LONG).show();
                }

                // Update member spinner when a list is selected
                if (listSpinner != null) {
                    listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            updateMemberSpinner(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            memberNames.clear();
                            memberIds.clear();
                            if (memberSpinner != null) {
                                memberSpinner.setAdapter(null);
                            }
                        }
                    });
                }

                // Initially populate member spinner if a list is selected
                if (!listNames.isEmpty()) {
                    updateMemberSpinner(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load lists: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMemberSpinner(int listPosition) {
        String selectedListId = listIds.get(listPosition);
        List<String> memberIdsInList = listMembersMap.get(selectedListId);

        List<String> filteredMemberNames = new ArrayList<>();
        List<String> filteredMemberIds = new ArrayList<>();

        for (int i = 0; i < memberIds.size(); i++) {
            if (memberIdsInList != null && memberIdsInList.contains(memberIds.get(i))) {
                filteredMemberNames.add(memberNames.get(i));
                filteredMemberIds.add(memberIds.get(i));
            }
        }

        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, filteredMemberNames);
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (memberSpinner != null) {
            memberSpinner.setAdapter(memberAdapter);
            memberSpinner.setTag(filteredMemberIds);
        } else {
            Toast.makeText(getContext(), "memberSpinner is null", Toast.LENGTH_LONG).show();
        }
    }

    private void addGiftToMember() {
        if (listSpinner == null || memberSpinner == null || listSpinner.getSelectedItem() == null || memberSpinner.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a list and member", Toast.LENGTH_SHORT).show();
            return;
        }

        int listPosition = listSpinner.getSelectedItemPosition();
        int memberPosition = memberSpinner.getSelectedItemPosition();

        String listId = listIds.get(listPosition);
        @SuppressWarnings("unchecked")
        List<String> filteredMemberIds = (List<String>) memberSpinner.getTag();
        String memberId = filteredMemberIds.get(memberPosition);

        // Create a new gift entry
        Map<String, Object> giftMap = new HashMap<>();
        giftMap.put("name", deal.getDeal_title());
        String price = (deal.getDeal_price() != null && deal.getDeal_price().getAmount() != null)
                ? deal.getDeal_price().getAmount()
                : "0.00";
        giftMap.put("price", price);
        giftMap.put("website", deal.getDeal_url());
        giftMap.put("notes", "");
        giftMap.put("status", "Idea");

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference giftRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listId)
                .child("members")
                .child(memberId)
                .child("gifts");

        String giftId = giftRef.push().getKey();
        if (giftId == null) {
            Toast.makeText(getContext(), "Failed to create gift ID", Toast.LENGTH_SHORT).show();
            return;
        }

        giftMap.put("key", giftId);

        giftRef.child(giftId).setValue(giftMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "🎁 Gift added to member!", Toast.LENGTH_SHORT).show();
                    // Pop back to the DiscountsFragment
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add gift: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}