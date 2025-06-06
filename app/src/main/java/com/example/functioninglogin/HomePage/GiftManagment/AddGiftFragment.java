package com.example.functioninglogin.HomePage.GiftManagment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddGiftFragment extends Fragment {

    private String listKey, memberKey;

    private EditText giftNameEdit, giftPriceEdit, giftWebsiteEdit, giftNotesEdit;
    private RadioGroup statusGroup;
    private ImageView addGiftImage;
    private Uri selectedImageUri;
    private StorageReference storageRef;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    addGiftImage.setImageURI(selectedImageUri);
                }
            });

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
        addGiftImage = view.findViewById(R.id.addGiftImage);
        Button saveGiftButton = view.findViewById(R.id.saveGiftButton);

        storageRef = FirebaseStorage.getInstance().getReference("gift_images");

        addGiftImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

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

        if (selectedImageUri != null) {
            String fileName = "gift_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        saveGiftToDatabase(giftRef, giftId, name, price, website, notes, status, downloadUri.toString());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            saveGiftToDatabase(giftRef, giftId, name, price, website, notes, status, "");
        }
    }

    private void saveGiftToDatabase(DatabaseReference giftRef, String giftId, String name, String price, String website, String notes, String status, String imageUrl) {
        Map<String, Object> giftMap = new HashMap<>();
        giftMap.put("name", name);
        giftMap.put("price", price);
        giftMap.put("website", website);
        giftMap.put("notes", notes);
        giftMap.put("status", status);
        giftMap.put("imageUrl", imageUrl);
        giftMap.put("key", giftId);

        giftRef.child(giftId).setValue(giftMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "🎁 Gift added!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save gift", Toast.LENGTH_SHORT).show());
    }

    private String getStatusFromRadio() {
        int id = statusGroup.getCheckedRadioButtonId();
        if (id == R.id.radioIdea) return "Idea";
        else if (id == R.id.radioBought) return "Bought";
        else if (id == R.id.radioArrived) return "Arrived";
        else if (id == R.id.radioWrapped) return "Wrapped";
        return "Idea"; // default fallback
    }

    private void clearForm() {
        giftNameEdit.setText("");
        giftPriceEdit.setText("");
        giftWebsiteEdit.setText("");
        giftNotesEdit.setText("");
        statusGroup.check(R.id.radioIdea);
        addGiftImage.setImageResource(R.drawable.baseline_account_box_24);
        selectedImageUri = null;
    }
}
