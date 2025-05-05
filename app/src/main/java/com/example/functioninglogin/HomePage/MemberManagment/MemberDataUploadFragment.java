package com.example.functioninglogin.HomePage.MemberManagment;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class MemberDataUploadFragment extends Fragment {

    private ImageView memberImageView;
    private EditText memberNameEditText, memberRoleEditText;
    private Button saveMemberButton;

    private Uri selectedImageUri;
    private String imageUrl;
    private DatabaseReference memberRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static MemberDataUploadFragment newInstance(String listKey) {
        MemberDataUploadFragment fragment = new MemberDataUploadFragment();
        Bundle args = new Bundle();
        args.putString("listKey", listKey);
        fragment.setArguments(args);
        return fragment;
    }

    public MemberDataUploadFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_upload, container, false);

        String listKey = getArguments() != null ? getArguments().getString("listKey") : null;
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (listKey == null || userId == null) {
            Toast.makeText(requireContext(), "Missing list reference", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return view;
        }

        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID") // ✅ NEW STRUCTURE
                .child(userId)
                .child("lists")
                .child(listKey)
                .child("members");

        initUI(view);
        setupImagePicker();

        memberImageView.setOnClickListener(v -> openImagePicker());
        saveMemberButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadMemberImage();
            } else {
                saveMemberData(""); // Fallback with no image
            }
        });

        return view;
    }

    private void initUI(View view) {
        memberImageView = view.findViewById(R.id.memberImageView);
        memberNameEditText = view.findViewById(R.id.memberNameEditText);
        memberRoleEditText = view.findViewById(R.id.memberRoleEditText);
        saveMemberButton = view.findViewById(R.id.saveMemberButton);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        memberImageView.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent pickerIntent = new Intent(Intent.ACTION_PICK);
        pickerIntent.setType("image/*");
        imagePickerLauncher.launch(pickerIntent);
    }

    private void uploadMemberImage() {
        AlertDialog dialog = showProgressDialog();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("Member Images")
                .child(Objects.requireNonNull(selectedImageUri.getLastPathSegment()));

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
            imageUrl = uri.toString();
            dialog.dismiss();
            saveMemberData(imageUrl);
        })).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveMemberData(String imageUrl) {
        String name = memberNameEditText.getText().toString().trim();
        String role = memberRoleEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = memberRef.push().getKey();
        MemberDataClass member = new MemberDataClass(name, role, imageUrl);
        member.setKey(key);

        memberRef.child(Objects.requireNonNull(key)).setValue(member).addOnSuccessListener(unused -> {
            Toast.makeText(requireContext(), "Member added!", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add member: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private AlertDialog showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
