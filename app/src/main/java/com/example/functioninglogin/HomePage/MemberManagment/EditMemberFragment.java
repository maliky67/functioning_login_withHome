package com.example.functioninglogin.HomePage.MemberManagment;

import android.app.Activity;
import android.app.ProgressDialog;
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

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class EditMemberFragment extends Fragment {

    private ImageView memberImageView;
    private EditText nameEditText, roleEditText;
    private DatabaseReference memberRef;
    private ProgressDialog progressDialog;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static EditMemberFragment newInstance(String listKey, String memberKey) {
        EditMemberFragment fragment = new EditMemberFragment();
        Bundle args = new Bundle();
        args.putString("listKey", listKey);
        args.putString("memberKey", memberKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_member, container, false);

        // 🔗 Firebase reference setup
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        assert getArguments() != null;
        String listKey = getArguments().getString("listKey");
        String memberKey = getArguments().getString("memberKey");

        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(Objects.requireNonNull(listKey))
                .child("members")
                .child(Objects.requireNonNull(memberKey));

        storageRef = FirebaseStorage.getInstance().getReference("member_images");

        // 📦 Bind Views
        memberImageView = view.findViewById(R.id.editmemberImageView);
        nameEditText = view.findViewById(R.id.editmemberNameEditText);
        roleEditText = view.findViewById(R.id.editmemberRoleEditText);
        Button saveButton = view.findViewById(R.id.editMemberButton);

        // 📷 Register image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        memberImageView.setImageURI(selectedImageUri);
                        uploadImageToFirebase(selectedImageUri);
                    }
                });

        // 📸 Image click opens gallery
        memberImageView.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            imagePickerLauncher.launch(pickImage);
        });

        // ⏳ Setup progress dialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);

        // 🔄 Load current member data
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MemberDataClass member = snapshot.getValue(MemberDataClass.class);
                if (member != null) {
                    nameEditText.setText(member.getName());
                    roleEditText.setText(member.getRole());

                    if (member.getImageUrl() != null && !member.getImageUrl().isEmpty()) {
                        Glide.with(requireContext()).load(member.getImageUrl()).into(memberImageView);
                    } else {
                        memberImageView.setImageResource(R.drawable.baseline_account_box_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "❌ Failed to load member", Toast.LENGTH_SHORT).show();
            }
        });

        // 💾 Save button logic
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newRole = roleEditText.getText().toString().trim();

            if (newName.isEmpty()) {
                nameEditText.setError("Name required");
                return;
            }

            progressDialog.show();

            memberRef.child("name").setValue(newName);
            memberRef.child("role").setValue(newRole)
                    .addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "✅ Member updated", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "❌ Failed to save", Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private void uploadImageToFirebase(Uri uri) {
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        String fileName = "member_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    memberRef.child("imageUrl").setValue(downloadUri.toString());
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "📸 Image updated", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "❌ Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }
}
