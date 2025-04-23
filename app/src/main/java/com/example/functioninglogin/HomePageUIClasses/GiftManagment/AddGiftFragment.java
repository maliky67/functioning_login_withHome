package com.example.functioninglogin.HomePageUIClasses.GiftManagment;

import android.os.Bundle;
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

public class AddGiftFragment extends Fragment {

    private String listKey, memberKey;

    private EditText giftNameEdit, giftPriceEdit, giftWebsiteEdit, giftNotesEdit;
    private RadioGroup statusGroup;

    public static AddGiftFragment newInstance(String listKey, String memberKey) {
        AddGiftFragment fragment = new AddGiftFragment();
        Bundle args = new Bundle();
        args.putString("listKey", listKey);
        args.putString("memberKey", memberKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_gift_fragment, container, false);

        // 🧩 Get list & member keys from args
        if (getArguments() != null) {
            listKey = getArguments().getString("listKey");
            memberKey = getArguments().getString("memberKey");
        }

        // ❗ Validate input
        if (listKey == null || memberKey == null) {
            Toast.makeText(requireContext(), "Missing list or member ID!", Toast.LENGTH_SHORT).show();
            return new FrameLayout(requireContext());
        }

        // 🔗 Bind UI
        statusGroup = view.findViewById(R.id.statusGroup);
        giftNameEdit = view.findViewById(R.id.giftNameEdit);
        giftPriceEdit = view.findViewById(R.id.giftPriceEdit);
        giftWebsiteEdit = view.findViewById(R.id.giftWebsiteEdit);
        giftNotesEdit = view.findViewById(R.id.giftNotesEdit);
        Button saveGiftButton = view.findViewById(R.id.saveGiftButton);

        saveGiftButton.setOnClickListener(v -> uploadGiftToFirebase());

        return view;
    }

    private void uploadGiftToFirebase() {
        String name = giftNameEdit.getText().toString().trim();
        String price = giftPriceEdit.getText().toString().trim();
        String website = giftWebsiteEdit.getText().toString().trim();
        String notes = giftNotesEdit.getText().toString().trim();
        String status = getStatusFromRadio();

        if (name.isEmpty() || price.isEmpty()) {
            Toast.makeText(requireContext(), "Name and price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference giftRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members")
                .child(memberKey)
                .child("gifts");

        String giftId = giftRef.push().getKey();

        if (giftId == null) {
            Toast.makeText(requireContext(), "Failed to create gift ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> giftMap = new HashMap<>();
        giftMap.put("name", name);
        giftMap.put("price", price);
        giftMap.put("website", website);
        giftMap.put("notes", notes);
        giftMap.put("status", status);
        giftMap.put("key", giftId);

        giftRef.child(giftId).setValue(giftMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "🎁 Gift added!", Toast.LENGTH_SHORT).show();
                    // Go back to member screen
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_fragment_container, MemberViewFragment.newInstance(listKey, memberKey))
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save gift: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getStatusFromRadio() {
        int id = statusGroup.getCheckedRadioButtonId();
        if (id == R.id.radioIdea) return "Idea";
        else if (id == R.id.radioBought) return "Bought";
        else if (id == R.id.radioArrived) return "Arrived";
        else if (id == R.id.radioWrapped) return "Wrapped";
        return "Idea"; // default fallback
    }
}
