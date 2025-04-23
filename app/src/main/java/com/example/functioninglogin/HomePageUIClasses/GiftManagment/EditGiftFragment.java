package com.example.functioninglogin.HomePageUIClasses.GiftManagment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberViewFragment;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditGiftFragment extends Fragment {

    private String listKey, memberKey, giftKey;
    private EditText nameEdit, priceEdit, websiteEdit, notesEdit;
    private RadioGroup statusGroup;
    private Button editGiftButton;

    public static EditGiftFragment newInstance(String listKey, String memberKey, String giftKey) {
        EditGiftFragment fragment = new EditGiftFragment();
        Bundle args = new Bundle();
        args.putString("listKey", listKey);
        args.putString("memberKey", memberKey);
        args.putString("giftKey", giftKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_gift_fragment, container, false);

        // 🔐 Get arguments safely
        if (getArguments() != null) {
            listKey = getArguments().getString("listKey");
            memberKey = getArguments().getString("memberKey");
            giftKey = getArguments().getString("giftKey");
        }

        // 🔧 UI binding
        nameEdit = view.findViewById(R.id.editgiftNameEdit);
        priceEdit = view.findViewById(R.id.editgiftPriceEdit);
        websiteEdit = view.findViewById(R.id.editgiftWebsiteEdit);
        notesEdit = view.findViewById(R.id.editgiftNotesEdit);
        statusGroup = view.findViewById(R.id.editstatusGroup);
        editGiftButton = view.findViewById(R.id.editGiftButton);

        loadGiftData();

        editGiftButton.setOnClickListener(v -> {
            editGiftButton.setEnabled(false); // Prevent double-tap
            saveGiftData();
        });

        return view;
    }

    private void loadGiftData() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference giftRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts")
                .child(giftKey);

        giftRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GiftItem gift = snapshot.getValue(GiftItem.class);
                if (gift != null) {
                    nameEdit.setText(gift.getName());
                    priceEdit.setText(gift.getPrice());
                    websiteEdit.setText(gift.getWebsite());
                    notesEdit.setText(gift.getNotes());

                    String status = gift.getStatus();
                    if (status != null) {
                        switch (status.toLowerCase()) {
                            case "bought":
                                statusGroup.check(R.id.editradioBought);
                                break;
                            case "arrived":
                                statusGroup.check(R.id.editradioArrived);
                                break;
                            case "wrapped":
                                statusGroup.check(R.id.editradioWrapped);
                                break;
                            default:
                                statusGroup.check(R.id.editradioIdea);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "❌ Failed to load gift", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveGiftData() {
        String name = nameEdit.getText().toString().trim();
        String price = priceEdit.getText().toString().trim();
        String website = websiteEdit.getText().toString().trim();
        String notes = notesEdit.getText().toString().trim();
        int checkedId = statusGroup.getCheckedRadioButtonId();

        String status = "Idea";
        if (checkedId == R.id.editradioBought) status = "Bought";
        else if (checkedId == R.id.editradioArrived) status = "Arrived";
        else if (checkedId == R.id.editradioWrapped) status = "Wrapped";

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference giftRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts")
                .child(giftKey);

        Map<String, Object> updatedGift = new HashMap<>();
        updatedGift.put("name", name);
        updatedGift.put("price", price);
        updatedGift.put("website", website);
        updatedGift.put("notes", notes);
        updatedGift.put("status", status);

        giftRef.updateChildren(updatedGift)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "🎁 Gift updated", Toast.LENGTH_SHORT).show();

                    // Optional: small delay so UI update is smooth
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        MemberViewFragment fragment = MemberViewFragment.newInstance(listKey, memberKey);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.home_fragment_container, fragment)
                                .commitAllowingStateLoss(); // safer for fragment transition
                    }, 300);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "❌ Failed to update", Toast.LENGTH_SHORT).show();
                    editGiftButton.setEnabled(true); // Re-enable if failed
                });
    }
}
