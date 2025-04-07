package com.example.functioninglogin.HomePageUIClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.functioninglogin.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

public class Upload_Data extends AppCompatActivity {

    private ImageView imageViewUpload;
    private EditText editTextTopic, editTextDesc;
    private Button buttonSave;

    private Uri selectedImageUri;
    private String imageURL;

    private DatabaseReference databaseReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);

        // 🔧 Initialize Views
        imageViewUpload = findViewById(R.id.uploadImage);
        editTextTopic = findViewById(R.id.uploadTopic);
        editTextDesc = findViewById(R.id.uploadDesc);
        buttonSave = findViewById(R.id.saveButton);

        // 🔗 Firebase DB path
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId);

        // 🖼️ Setup image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageViewUpload.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        imageViewUpload.setOnClickListener(v -> openImagePicker());

        buttonSave.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageToFirebase();
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent pickImage = new Intent(Intent.ACTION_PICK);
        pickImage.setType("image/*");
        imagePickerLauncher.launch(pickImage);
    }

    private void uploadImageToFirebase() {
        AlertDialog dialog = showProgressDialog();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("Android Images")
                .child(Objects.requireNonNull(selectedImageUri.getLastPathSegment()));

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            imageURL = task.getResult().toString();
                            saveDataToDatabase(dialog);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDataToDatabase(AlertDialog dialog) {
        String title = editTextTopic.getText().toString().trim();
        String desc = editTextDesc.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            dialog.dismiss();
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        // ✅ Use imageURL directly (it's a String now)
        DataHelperClass data = new DataHelperClass(title, desc, imageURL);
        data.setKey(title);

        databaseReference.child(title).setValue(data)
                .addOnSuccessListener(unused -> {
                    databaseReference.child(title).child("timestamp").setValue(currentDate);
                    dialog.dismiss();
                    Toast.makeText(this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
