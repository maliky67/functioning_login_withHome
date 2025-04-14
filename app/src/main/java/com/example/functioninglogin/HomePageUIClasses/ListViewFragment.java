package com.example.functioninglogin.HomePageUIClasses;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab, deleteFab;
    private List<MemberDataClass> memberList;
    private MemberAdapter memberAdapter;
    private DatabaseReference memberRef;
    private ValueEventListener memberListener;
    private String listKey;

    private TextView headerTitle, headerDesc;
    private ImageView headerImage;

    public ListViewFragment() {}

    public static ListViewFragment newInstance(String key, String title, String desc, String imageUrl) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString("Key", key);
        args.putString("Title", title);
        args.putString("Description", desc);
        args.putString("Image", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

        headerTitle = view.findViewById(R.id.headerTitle);
        headerDesc = view.findViewById(R.id.headerDesc);
        headerImage = view.findViewById(R.id.headerImage);
        recyclerView = view.findViewById(R.id.ListrecyclerView);
        fab = view.findViewById(R.id.memberfab);
        deleteFab = view.findViewById(R.id.memberdeletefab);

        // Load bundle arguments
        if (getArguments() != null) {
            listKey = getArguments().getString("Key", "");
            headerTitle.setText(getArguments().getString("Title", "List Title"));
            headerDesc.setText(getArguments().getString("Description", "Description"));

            String imageUrl = getArguments().getString("Image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext()).load(imageUrl).into(headerImage);
            } else {
                headerImage.setImageResource(R.drawable.baseline_ac_unit_24);
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(requireContext(), memberList, listKey);
        recyclerView.setAdapter(memberAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                MemberDataClass memberToDelete = memberList.get(position);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Member?")
                        .setMessage("Are you sure you want to delete \"" + memberToDelete.getName() + "\" and all their gifts?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteMemberFromFirebase(memberToDelete.getKey());
                            memberList.remove(position);
                            memberAdapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            memberAdapter.notifyItemChanged(position); // Rebind if cancelled
                        })
                        .show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members"); // ✅ NEW PATH

        // 👥 Fetch members + gifts
        memberListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberList.clear();
                for (DataSnapshot memberSnap : snapshot.getChildren()) {
                    MemberDataClass member = memberSnap.getValue(MemberDataClass.class);
                    if (member != null) {
                        member.setKey(memberSnap.getKey());

                        DataSnapshot giftsSnap = memberSnap.child("gifts");
                        if (giftsSnap.exists()) {
                            Map<String, GiftItem> giftMap = new HashMap<>();
                            for (DataSnapshot giftSnap : giftsSnap.getChildren()) {
                                GiftItem gift = giftSnap.getValue(GiftItem.class);
                                if (gift != null) {
                                    giftMap.put(giftSnap.getKey(), gift);
                                }
                            }
                            member.setGifts(giftMap);
                        }

                        memberList.add(member);
                    }
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

        deleteFab.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Entire List?")
                    .setMessage("This will permanently delete the list and all its members. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEntireList())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void deleteEntireList() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference listRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey); // ✅ NEW DELETE PATH

        listRef.removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "List deleted successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void deleteMemberFromFirebase(String memberKey) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference memberNode = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey);

        memberNode.removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(requireContext(), "Member deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error deleting member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
