package com.example.functioninglogin.HomePage.MemberManagment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePage.GiftManagment.AddGiftFragment;
import com.example.functioninglogin.HomePage.GiftManagment.GiftDisplayAdapter;
import com.example.functioninglogin.HomePage.GiftManagment.GiftItem;
import com.example.functioninglogin.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class MemberViewFragment extends Fragment {

    private String listKey, memberKey;
    private TextView memberName, memberRole, memberTotalSpent;
    private ImageView memberImage;

    private final List<GiftItem> giftList = new ArrayList<>();
    private GiftDisplayAdapter giftAdapter;

    public static MemberViewFragment newInstance(String listKey, String memberKey) {
        MemberViewFragment fragment = new MemberViewFragment();
        Bundle args = new Bundle();
        args.putString("listKey", listKey);
        args.putString("memberKey", memberKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_view, container, false);

        Bundle args = getArguments();
        if (args != null) {
            listKey = args.getString("listKey");
            memberKey = args.getString("memberKey");
        }

        if (listKey == null || memberKey == null) {
            Toast.makeText(requireContext(), "❌ Missing list or member ID", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 🔗 Bind UI
        memberName = view.findViewById(R.id.memberName);
        memberRole = view.findViewById(R.id.memberRole);
        memberImage = view.findViewById(R.id.memberImage);
        memberTotalSpent = view.findViewById(R.id.memberTotalSpent);
        RecyclerView giftRecyclerView = view.findViewById(R.id.giftRecyclerView);
        MaterialButton addGiftButton = view.findViewById(R.id.addGiftButton);
        CardView memberHeaderCard = view.findViewById(R.id.memberHeaderCard);

        // ➕ Add gift
        addGiftButton.setOnClickListener(v -> {
            AddGiftFragment fragment = AddGiftFragment.newInstance(listKey, memberKey);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ✏️ Edit member
        memberHeaderCard.setOnClickListener(v -> {
            EditMemberFragment fragment = EditMemberFragment.newInstance(listKey, memberKey);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ♻️ Recycler setup
        giftRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        giftAdapter = new GiftDisplayAdapter(requireContext(), giftList, listKey, memberKey);
        giftRecyclerView.setAdapter(giftAdapter);

        // 🧹 Swipe-to-delete gift
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (position < 0 || position >= giftList.size()) {
                    giftAdapter.notifyItemChanged(position);
                    return;
                }

                GiftItem toDelete = giftList.get(position);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Gift?")
                        .setMessage("Are you sure you want to delete \"" + toDelete.getName() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (toDelete.getKey() != null) {
                                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                                FirebaseDatabase.getInstance()
                                        .getReference("Unique User ID")
                                        .child(userId)
                                        .child("lists")
                                        .child(listKey)
                                        .child("members")
                                        .child(memberKey)
                                        .child("gifts")
                                        .child(toDelete.getKey())
                                        .removeValue()
                                        .addOnSuccessListener(unused -> {
                                            // ✅ Defensive check
                                            if (giftList.contains(toDelete)) {
                                                int safeIndex = giftList.indexOf(toDelete);
                                                giftList.remove(safeIndex);
                                                giftAdapter.notifyItemRemoved(safeIndex);
                                            }
                                            Toast.makeText(requireContext(), "Gift deleted", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(), "Failed to delete gift", Toast.LENGTH_SHORT).show();
                                            giftAdapter.notifyItemChanged(position); // rollback
                                        });
                            } else {
                                giftList.remove(position);
                                giftAdapter.notifyItemRemoved(position);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            giftAdapter.notifyItemChanged(position); // rollback
                            dialog.dismiss();
                        })
                        .setCancelable(false)
                        .show();
            }

        }).attachToRecyclerView(giftRecyclerView);

        // 🔄 Firebase loads
        loadMemberInfo();
        loadGiftData();

        return view;
    }

    private void loadMemberInfo() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MemberDataClass member = snapshot.getValue(MemberDataClass.class);
                if (member != null) {
                    memberName.setText(member.getName());
                    memberRole.setText(member.getRole());

                    if (member.getImageUrl() != null && !member.getImageUrl().isEmpty()) {
                        Glide.with(requireContext()).load(member.getImageUrl()).into(memberImage);
                    } else {
                        memberImage.setImageResource(R.drawable.baseline_account_box_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "❌ Error loading member", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGiftData() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                giftList.clear();
                double totalSpent = 0;

                for (DataSnapshot giftSnap : snapshot.getChildren()) {
                    GiftItem gift = giftSnap.getValue(GiftItem.class);
                    if (gift != null) {
                        gift.setKey(giftSnap.getKey());
                        giftList.add(gift);
                        try {
                            totalSpent += Double.parseDouble(gift.getPrice());
                        } catch (Exception ignored) {}
                    }
                }

                memberTotalSpent.setText(String.format("$%.2f", totalSpent));
                giftAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "❌ Error loading gifts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
