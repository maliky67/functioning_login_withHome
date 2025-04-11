package com.example.functioninglogin.HomePageUIClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

public class UpdateFragment extends Fragment {

    private ImageView updateImage;
    private Button updateButton;
    private EditText updateTitle, updateDesc, updateGiftIdea, updatePrice;

    private String type, key, listKey, imageUrl, oldImageURL;
    private Uri uri = null;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> imagePicker;

    public static UpdateFragment newInstance(String key, String listKey, String type, String imageUrl) {
        UpdateFragment fragment = new UpdateFragment();
        Bundle args = new Bundle();
        args.putString("Key", key);
        args.putString("listKey", listKey);
        args.putString("type", type);
        args.putString("Image", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update, container, false);

        // Bind views
        updateImage = view.findViewById(R.id.updateImage);
        updateButton = view.findViewById(R.id.updateButton);
        updateTitle = view.findViewById(R.id.updateTitle);
        updateDesc = view.findViewById(R.id.updateDesc);
        updateGiftIdea = view.findViewById(R.id.updateLang);
        updatePrice = view.findViewById(R.id.updatePrice);

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getString("type", "member");
            key = bundle.getString("Key");
            listKey = bundle.getString("listKey");
            oldImageURL = bundle.getString("Image");
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if ("member".equals(type)) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Unique User ID")
                    .child(uid)
                    .child(listKey)
                    .child("members")
                    .child(key);

            // Prepopulate existing data
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(requireContext(), "Member not found", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                        return;
                    }

                    String name = snapshot.child("name").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String giftIdea = snapshot.child("giftIdea").getValue(String.class);
                    String price = snapshot.child("price").getValue(String.class);
                    imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    updateTitle.setText(name != null ? name : "");
                    updateDesc.setText(role != null ? role : "");
                    updateGiftIdea.setText(giftIdea != null ? giftIdea : "");
                    updatePrice.setText(price != null ? price : "");

                    Glide.with(requireContext()).load(imageUrl).into(updateImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(), "Failed to load member", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Image picker
        setupImagePicker();
        updateImage.setOnClickListener(v -> openImagePicker());

        updateButton.setOnClickListener(v -> {
            if (uri != null) {
                uploadNewImage();
            } else {
                updateData(imageUrl);
            }
        });

        return view;
    }

    private void setupImagePicker() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        updateImage.setImageURI(uri);
                    } else {
                        Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent pickImage = new Intent(Intent.ACTION_PICK);
        pickImage.setType("image/*");
        imagePicker.launch(pickImage);
    }

    private void uploadNewImage() {
        storageReference = FirebaseStorage.getInstance()
                .getReference("Android Images")
                .child(uri.getLastPathSegment());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setView(R.layout.progress_layout)
                .create();
        dialog.show();

        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot ->
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    dialog.dismiss();
                    updateData(downloadUri.toString());
                })
        ).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateData(String newImageUrl) {
        String name = updateTitle.getText().toString().trim();
        String role = updateDesc.getText().toString().trim();
        String gift = updateGiftIdea.getText().toString().trim();
        String price = updatePrice.getText().toString().trim();

        if (name.isEmpty() || role.isEmpty() || gift.isEmpty() || price.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        MemberDataClass updated = new MemberDataClass(name, role, newImageUrl, gift, price);

        databaseReference.setValue(updated).addOnSuccessListener(unused -> {
            deleteOldImage();
            Toast.makeText(requireContext(), "Member updated", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteOldImage() {
        if (uri != null && oldImageURL != null && !oldImageURL.isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(oldImageURL).delete();
        }
    }
}
