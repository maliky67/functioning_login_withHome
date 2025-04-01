package com.example.functioninglogin.HomePageUIClasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailActivity extends AppCompatActivity {

    TextView detailDesc, detailTitle, detailLang;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        detailDesc = findViewById(R.id.detailDesc);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        detailLang = findViewById(R.id.detailLang);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailDesc.setText(bundle.getString("Description"));
            detailTitle.setText(bundle.getString("Title"));
            detailLang.setText(bundle.getString("Language"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }

        deleteButton.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(DetailActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Android Tutorials");

            if (imageUrl == null || imageUrl.isEmpty()) {
                Log.w("FirebaseStorage", "No image URL found, skipping storage deletion.");
                reference.child(uid).child(key).removeValue();
                Toast.makeText(DetailActivity.this, "Deleted (no image found)", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Store_Data_Realtime.class));
                finish();
                return;
            }

            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

            storageReference.getMetadata()
                    .addOnSuccessListener(storageMetadata -> {
                        // File exists, go ahead and delete it
                        storageReference.delete()
                                .addOnSuccessListener(unused -> {
                                    reference.child(uid).child(key).removeValue();
                                    Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), Store_Data_Realtime.class));
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // File does not exist, just delete the database entry
                        Log.w("FirebaseStorage", "Image not found, deleting only DB node.");
                        reference.child(uid).child(key).removeValue();
                        Toast.makeText(DetailActivity.this, "Deleted (image missing)", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Store_Data_Realtime.class));
                        finish();
                    });
        });

        editButton.setOnClickListener(view -> {
            Intent intent = new Intent(DetailActivity.this, UpdateActivity.class)
                    .putExtra("Title", detailTitle.getText().toString())
                    .putExtra("Description", detailDesc.getText().toString())
                    .putExtra("Language", detailLang.getText().toString())
                    .putExtra("Image", imageUrl)
                    .putExtra("Key", key);
            startActivity(intent);
        });
    }
}
