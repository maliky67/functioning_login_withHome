package com.example.functioninglogin.HomePageUIClasses.ListManagment;

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
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberDataClass;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.Objects;

public class UpdateFragment extends Fragment {

    private ImageView updateImage;
    private EditText updateTitle, updateDesc;

    private String type, key, listKey, imageUrl, oldImageURL;
    private Uri uri = null;

    private DatabaseReference databaseReference;

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

        updateImage = view.findViewById(R.id.updateImage);
        Button updateButton = view.findViewById(R.id.updateButton);
        updateTitle = view.findViewById(R.id.updateTitle);
        updateDesc = view.findViewById(R.id.updateDesc);

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getString("type", "member");
            key = bundle.getString("Key");
            listKey = bundle.getString("listKey");
            oldImageURL = bundle.getString("Image");
        }

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        if ("member".equals(type)) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Unique User ID") // ✅ Corrected path
                    .child(uid)
                    .child("lists")
                    .child(listKey)
                    .child("members")
                    .child(key);

            loadExistingData();
        }

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

    private void loadExistingData() {
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
                imageUrl = snapshot.child("imageUrl").getValue(String.class);

                updateTitle.setText(name != null ? name : "");
                updateDesc.setText(role != null ? role : "");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(requireContext()).load(imageUrl).into(updateImage);
                } else {
                    updateImage.setImageResource(R.drawable.baseline_account_box_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load member: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupImagePicker() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        updateImage.setImageURI(uri);
                    } else {
                        Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePicker.launch(intent);
    }

    private void uploadNewImage() {
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("Member Images")
                .child(Objects.requireNonNull(uri.getLastPathSegment()));

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
            Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateData(String newImageUrl) {
        String name = updateTitle.getText().toString().trim();
        String role = updateDesc.getText().toString().trim();

        if (name.isEmpty() || role.isEmpty()) {
            Toast.makeText(requireContext(), "Name and role are required", Toast.LENGTH_SHORT).show();
            return;
        }

        MemberDataClass updated = new MemberDataClass(name, role, newImageUrl);
        updated.setKey(key);

        databaseReference.setValue(updated).addOnSuccessListener(unused -> {
            deleteOldImage();
            Toast.makeText(requireContext(), "Member updated", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteOldImage() {
        if (uri != null && oldImageURL != null && !oldImageURL.isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(oldImageURL).delete();
        }
    }
}
