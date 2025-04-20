package com.example.functioninglogin.HomePageUIClasses.MemberManagment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftDisplayAdapter;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class MemberViewFragment extends Fragment {

    private String listKey, memberKey;
    private TextView memberName, memberRole, memberTotalSpent;
    private ImageView memberImage;
    private RecyclerView giftRecyclerView;

    private List<GiftItem> giftList = new ArrayList<>();
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

        // 🚨 Step 1: Safe arg fetch
        Bundle args = getArguments();
        if (args != null) {
            listKey = args.getString("listKey");
            memberKey = args.getString("memberKey");
        }

        if (listKey == null || memberKey == null) {
            Toast.makeText(requireContext(), "❌ Error: Missing list or member ID", Toast.LENGTH_LONG).show();
            return new FrameLayout(requireContext()); // exit safely
        }

        View view = inflater.inflate(R.layout.fragment_member_view, container, false);

        // 🚀 Step 2: UI setup
        memberName = view.findViewById(R.id.memberName);
        memberRole = view.findViewById(R.id.memberRole);
        memberImage = view.findViewById(R.id.memberImage);
        memberTotalSpent = view.findViewById(R.id.memberTotalSpent);

        giftRecyclerView = view.findViewById(R.id.giftRecyclerView);
        giftRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        giftAdapter = new GiftDisplayAdapter(requireContext(), giftList);
        giftRecyclerView.setAdapter(giftAdapter);

        // ✅ Step 3: Data load
        loadMemberInfo();
        loadGiftData();

        return view;
    }

    private void loadMemberInfo() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Unique User ID")
                    .child(userId)
                    .child("lists")
                    .child(listKey)
                    .child("members")
                    .child(memberKey);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Toast.makeText(requireContext(), "❌ Failed to load member info", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "🔥 Member fetch error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadGiftData() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    Toast.makeText(requireContext(), "❌ Failed to load gift data", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "🔥 Gift fetch error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
