package com.example.functioninglogin.HomePageUIClasses.GiftManagment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class EditGiftFragment extends Fragment {

    private EditText giftNameEdit, giftPriceEdit, giftWebsiteEdit, giftNotesEdit;
    private RadioGroup statusGroup;
    private Button saveGiftButton;

    private String listKey, memberKey, giftKey;

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

        // 🔗 Extract arguments
        if (getArguments() != null) {
            listKey = getArguments().getString("listKey");
            memberKey = getArguments().getString("memberKey");
            giftKey = getArguments().getString("giftKey");
        }

        // 🧠 Bind views
        giftNameEdit = view.findViewById(R.id.editgiftNameEdit);
        giftPriceEdit = view.findViewById(R.id.editgiftPriceEdit);
        giftWebsiteEdit = view.findViewById(R.id.editgiftWebsiteEdit);
        giftNotesEdit = view.findViewById(R.id.editgiftNotesEdit);
        statusGroup = view.findViewById(R.id.editstatusGroup);
        saveGiftButton = view.findViewById(R.id.editGiftButton);

        // 📥 Load data
        loadGiftFromFirebase();

        // 💾 Save click
        saveGiftButton.setOnClickListener(v -> saveUpdatedGift());

        return view;
    }

    private void loadGiftFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts")
                .child(giftKey);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GiftItem gift = snapshot.getValue(GiftItem.class);
                if (gift != null) {
                    giftNameEdit.setText(gift.getName());
                    giftPriceEdit.setText(gift.getPrice());
                    giftWebsiteEdit.setText(gift.getWebsite());
                    giftNotesEdit.setText(gift.getNotes());

                    String status = gift.getStatus() != null ? gift.getStatus().toLowerCase() : "";
                    switch (status) {
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
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "❌ Failed to load gift", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUpdatedGift() {
        String name = giftNameEdit.getText().toString().trim();
        String price = giftPriceEdit.getText().toString().trim();
        String website = giftWebsiteEdit.getText().toString().trim();
        String notes = giftNotesEdit.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            giftNameEdit.setError("Name required");
            return;
        }

        String status = "Idea";
        int checkedId = statusGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.editradioBought) status = "Bought";
        else if (checkedId == R.id.editradioArrived) status = "Arrived";
        else if (checkedId == R.id.editradioWrapped) status = "Wrapped";

        GiftItem updatedGift = new GiftItem(name, price, website, notes, status);
        updatedGift.setKey(giftKey); // 🔑 important for consistency

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference giftRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts")
                .child(giftKey);

        giftRef.setValue(updatedGift)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Gift updated", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack(); // go back
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update gift", Toast.LENGTH_SHORT).show();
                });
    }
}
