package com.example.functioninglogin.HomePageUIClasses;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;

public class DetailActivity extends AppCompatActivity {

    TextView detailTitle, detailPreferences, detailGiftIdea, detailPrice;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;

    String key = "", imageUrl = "", type = "", listKey = "";

    DatabaseReference memberRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Bind views
        detailTitle = findViewById(R.id.detailTitle);
        detailPreferences = findViewById(R.id.detailPreferences);
        detailGiftIdea = findViewById(R.id.detailGiftIdea);
        detailPrice = findViewById(R.id.detailPrice);
        detailImage = findViewById(R.id.detailImage);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);

        // Get intent data
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            type = bundle.getString("type", "member");
            key = bundle.getString("Key");
            listKey = bundle.getString("listKey");

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            memberRef = FirebaseDatabase.getInstance()
                    .getReference("Unique User ID")
                    .child(uid)
                    .child(listKey)
                    .child("members")
                    .child(key);

            // 🔄 Real-time listener
            memberRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(DetailActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String name = snapshot.child("name").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String giftIdea = snapshot.child("giftIdea").getValue(String.class);
                    String price = snapshot.child("price").getValue(String.class);
                    imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    detailTitle.setText(name != null ? name : "No Name");
                    detailPreferences.setText(role != null ? role : "No Preferences");
                    detailGiftIdea.setText(giftIdea != null ? giftIdea : "No Gift Idea");
                    detailPrice.setText(price != null && !price.isEmpty() ? "$" + price : "$0");

                    if (!isFinishing() && !isDestroyed()) {
                        Glide.with(DetailActivity.this).load(imageUrl).into(detailImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DetailActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
                }
            });
        }

        deleteButton.setOnClickListener(v -> deleteEntry());
        editButton.setOnClickListener(v -> editEntry());
    }

    private void deleteEntry() {
        memberRef.removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            finishToProperScreen();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
        });

        if (imageUrl != null && !imageUrl.isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete();
        }
    }

    private void editEntry() {
        Intent intent = new Intent(DetailActivity.this, UpdateActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("Key", key);
        intent.putExtra("listKey", listKey);
        intent.putExtra("Image", imageUrl);
        startActivity(intent);
    }

    private void finishToProperScreen() {
        Intent back = new Intent(this, com.example.functioninglogin.ListViewPage.class);
        back.putExtra("Key", listKey);
        startActivity(back);
        finish();
    }
}
