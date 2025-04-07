package com.example.functioninglogin.HomePageUIClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class Member_Data_Upload extends AppCompatActivity {

    private ImageView memberImageView;
    private EditText memberNameEditText, memberRoleEditText, memberGiftIdeaEditText, memberPriceEditText;
    private Button saveMemberButton;

    private Uri selectedImageUri;
    private String imageUrl;

    private DatabaseReference memberRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String listKey;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_data_upload);

        listKey = getIntent().getStringExtra("listKey");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (listKey == null || userId == null) {
            Toast.makeText(this, "Missing list reference", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child(listKey)
                .child("members");

        initUI();
        setupImagePicker();

        memberImageView.setOnClickListener(v -> openImagePicker());
        saveMemberButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadMemberImage();
            } else {
                saveMemberData(""); // No image
            }
        });
    }

    private void initUI() {
        memberImageView = findViewById(R.id.memberImageView);
        memberNameEditText = findViewById(R.id.memberNameEditText);
        memberRoleEditText = findViewById(R.id.memberRoleEditText);
        memberGiftIdeaEditText = findViewById(R.id.uploadDescss);
        memberPriceEditText = findViewById(R.id.uploadDescss2);
        saveMemberButton = findViewById(R.id.saveMemberButton);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        memberImageView.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
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
        AlertDialog progressDialog = showProgressDialog();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("Member Images")
                .child(Objects.requireNonNull(selectedImageUri.getLastPathSegment()));

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrl = uri.toString();
                progressDialog.dismiss();
                saveMemberData(imageUrl);
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveMemberData(String imageUrl) {
        String name = memberNameEditText.getText().toString().trim();
        String role = memberRoleEditText.getText().toString().trim();
        String giftIdea = memberGiftIdeaEditText.getText().toString().trim();
        String price = memberPriceEditText.getText().toString().trim();

        if (name.isEmpty() || role.isEmpty() || giftIdea.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        MemberDataClass member = new MemberDataClass(name, role, imageUrl, giftIdea, price);

        memberRef.child(name).setValue(member).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Member added!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to add member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private AlertDialog showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
