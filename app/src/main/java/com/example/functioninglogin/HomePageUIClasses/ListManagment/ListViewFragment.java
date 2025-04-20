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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ListViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab, deleteFab;
    private List<MemberDataClass> memberList;
    private MemberAdapter memberAdapter;
    private DatabaseReference memberRef;
    private ValueEventListener memberListener;
    private String listKey;

    private TextView headerTitle, headerDesc, headerTotalSpent, headerTotalBudget;
    private ImageView headerImage;
    private CardView headerCard;

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

        headerCard = view.findViewById(R.id.headerCard);
        headerTitle = view.findViewById(R.id.headerTitle);
        headerDesc = view.findViewById(R.id.headerDesc);
        headerImage = view.findViewById(R.id.headerImage);
        headerTotalSpent = view.findViewById(R.id.headerTotalSpent);
        headerTotalBudget = view.findViewById(R.id.headerTotalBudget);
        recyclerView = view.findViewById(R.id.ListrecyclerView);
        fab = view.findViewById(R.id.memberfab);
        deleteFab = view.findViewById(R.id.memberdeletefab);

        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(requireContext(), memberList, listKey);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(memberAdapter);

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

        AtomicReference<String> userId = new AtomicReference<>(FirebaseAuth.getInstance().getCurrentUser().getUid());
        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId.get())
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
                                try {
                                    totalSpent += Double.parseDouble(gift.getPrice());
                                } catch (Exception ignored) {}
                            }
                        }
                        member.setGifts(giftMap);
                        memberList.add(member);
                        count++;
                    }
                }

                headerDesc.setText(count == 0 ? "No Members Yet" : (count == 1 ? "1 member" : count + " members"));
                headerTotalSpent.setText("Total Spent: $" + String.format("%.2f", totalSpent));

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

        deleteFab.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Entire List?")
                    .setMessage("This will permanently delete the list and all its members. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEntireList())
                    .setNegativeButton("Cancel", null)
                    .show();
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
                            memberList.remove(position);
                            memberAdapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            memberAdapter.notifyItemChanged(position);
                        })
                        .show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        getParentFragmentManager().setFragmentResultListener("refreshList", this, (requestKey, bundle) -> {
            if (bundle.getBoolean("refreshListHeader", false)) {
                // 🔁 Refresh header data
                userId.set(FirebaseAuth.getInstance().getCurrentUser().getUid());
                DatabaseReference listMetaRef = FirebaseDatabase.getInstance()
                        .getReference("Unique User ID")
                        .child(userId.get())
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
                    }
                });
            }
        });

        return view;
    }

    private void deleteEntireList() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "List deleted", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMemberFromFirebase(String memberKey) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Member deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error deleting member", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (memberRef != null && memberListener != null) {
            memberRef.removeEventListener(memberListener);
        }
    }
}
