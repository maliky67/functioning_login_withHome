package com.example.functioninglogin.HomePage.GiftManagment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.HomePage.MemberManagment.MemberViewFragment;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditGiftFragment extends Fragment {

    private static final int IMAGE_PICK_REQUEST = 101;

    private String listKey, memberKey, giftKey;
    private EditText nameEdit, priceEdit, websiteEdit, notesEdit;
    private RadioGroup statusGroup;
    private Button editGiftButton;
    private ImageView giftImageView;

    private String currentImageUrl = ""; // Initial image from DB
    private String uploadedImageUrl = ""; // From Firebase upload
    private Uri selectedImageUri = null;

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

        // 🔐 Get arguments
        if (getArguments() != null) {
            listKey = getArguments().getString("listKey");
            memberKey = getArguments().getString("memberKey");
            giftKey = getArguments().getString("giftKey");
        }

        // UI bindings
        nameEdit = view.findViewById(R.id.editgiftNameEdit);
        priceEdit = view.findViewById(R.id.editgiftPriceEdit);
        websiteEdit = view.findViewById(R.id.editgiftWebsiteEdit);
        notesEdit = view.findViewById(R.id.editgiftNotesEdit);
        statusGroup = view.findViewById(R.id.editstatusGroup);
        editGiftButton = view.findViewById(R.id.editGiftButton);
        giftImageView = view.findViewById(R.id.editGiftImage);

        loadGiftData();

        editGiftButton.setOnClickListener(v -> {
            editGiftButton.setEnabled(false);
            saveGiftData();
        });

        giftImageView.setOnClickListener(v -> showImageOptionsDialog());

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
                    currentImageUrl = gift.getImageUrl();

                    // 🖼 Load image
                    if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(currentImageUrl)
                                .placeholder(R.drawable.baseline_card_giftcard_24)
                                .error(R.drawable.baseline_clear_24)
                                .into(giftImageView);
                    } else {
                        giftImageView.setImageResource(R.drawable.baseline_clear_24);
                    }

                    // 🎯 Status
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

    private void showImageOptionsDialog() {
        String[] options = {"📷 Upload from device", "🔗 Enter image URL"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) openImagePicker();
            else if (which == 1) showImageUrlInputDialog();
        });
        builder.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    private void showImageUrlInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Image URL");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(currentImageUrl);
        builder.setView(input);

        builder.setPositiveButton("Set", (dialog, which) -> {
            uploadedImageUrl = input.getText().toString().trim();
            Glide.with(requireContext())
                    .load(uploadedImageUrl)
                    .into(giftImageView);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(requireContext()).load(selectedImageUri).into(giftImageView);
            uploadImageToFirebase(selectedImageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fileName = "giftImages/" + userId + "/" + System.currentTimeMillis() + ".jpg";

        FirebaseStorage.getInstance().getReference(fileName)
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            uploadedImageUrl = uri.toString();
                            Toast.makeText(getContext(), "✅ Image uploaded", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "❌ Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

        String finalImageUrl = !uploadedImageUrl.isEmpty() ? uploadedImageUrl : currentImageUrl;

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
        updatedGift.put("imageUrl", finalImageUrl);

        giftRef.updateChildren(updatedGift)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "🎁 Gift updated", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        MemberViewFragment fragment = MemberViewFragment.newInstance(listKey, memberKey);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.home_fragment_container, fragment)
                                .commitAllowingStateLoss();
                    }, 300);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "❌ Failed to update", Toast.LENGTH_SHORT).show();
                    editGiftButton.setEnabled(true);
                });
    }
}
