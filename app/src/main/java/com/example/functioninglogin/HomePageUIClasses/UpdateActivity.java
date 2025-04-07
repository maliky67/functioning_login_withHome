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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UpdateActivity extends AppCompatActivity {

    ImageView updateImage;
    Button updateButton;
    EditText updateTitle, updateDesc, updateGiftIdea, updatePrice;

    String type, key, listKey, imageUrl, oldImageURL;
    Uri uri = null;

    DatabaseReference databaseReference;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // View bindings
        updateImage = findViewById(R.id.updateImage);
        updateButton = findViewById(R.id.updateButton);
        updateTitle = findViewById(R.id.updateTitle);
        updateDesc = findViewById(R.id.updateDesc);
        updateGiftIdea = findViewById(R.id.updateLang);
        updatePrice = findViewById(R.id.updatePrice);

        // Intent data
        Bundle bundle = getIntent().getExtras();
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

            // 🔄 Real-time Firebase listener
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(UpdateActivity.this, "Member not found", Toast.LENGTH_SHORT).show();
                        finish();
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

                    if (!isFinishing() && !isDestroyed()) {
                        Glide.with(UpdateActivity.this).load(imageUrl).into(updateImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateActivity.this, "Failed to load member", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Image picker
        ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        updateImage.setImageURI(uri);
                    } else {
                        Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        updateImage.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            imagePicker.launch(pickImage);
        });

        updateButton.setOnClickListener(v -> {
            if (uri != null) {
                uploadNewImage();
            } else {
                updateData(imageUrl);
            }
        });
    }

    private void uploadNewImage() {
        storageReference = FirebaseStorage.getInstance()
                .getReference("Android Images")
                .child(uri.getLastPathSegment());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                dialog.dismiss();
                updateData(uri.toString());
            });
        }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateData(String newImageUrl) {
        String name = updateTitle.getText().toString().trim();
        String role = updateDesc.getText().toString().trim();
        String gift = updateGiftIdea.getText().toString().trim();
        String price = updatePrice.getText().toString().trim();

        if (name.isEmpty() || role.isEmpty() || gift.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        MemberDataClass updated = new MemberDataClass(name, role, newImageUrl, gift, price);

        databaseReference.setValue(updated).addOnSuccessListener(unused -> {
            deleteOldImage();
            Toast.makeText(this, "Member updated", Toast.LENGTH_SHORT).show();
            finish(); // Go back to DetailActivity
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteOldImage() {
        if (uri != null && oldImageURL != null && !oldImageURL.isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(oldImageURL).delete();
        }
    }
}
