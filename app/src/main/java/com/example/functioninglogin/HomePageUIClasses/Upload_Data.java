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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

public class Upload_Data extends AppCompatActivity {

    private ImageView imageViewUpload;
    private EditText editTextTopic, editTextDesc, editTextLang;
    private Button buttonSave;

    private Uri selectedImageUri;
    private String imageURL;

    private DatabaseReference databaseReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);

        initializeUI();
        initializeFirebase();
        setupImagePicker();

        imageViewUpload.setOnClickListener(view -> openImagePicker());
        buttonSave.setOnClickListener(view -> {
            if (selectedImageUri != null) {
                uploadImageToFirebase();
            } else {
                Toast.makeText(Upload_Data.this, "Select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeUI() {
        imageViewUpload = findViewById(R.id.uploadImage);
        editTextTopic = findViewById(R.id.uploadTopic);
        editTextDesc = findViewById(R.id.uploadDesc);
        editTextLang = findViewById(R.id.uploadLang);
        buttonSave = findViewById(R.id.saveButton);
    }

    private void initializeFirebase() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Android Tutorials");
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageViewUpload.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(Upload_Data.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        imagePickerLauncher.launch(photoPicker);
    }

    private void uploadImageToFirebase() {
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("Android Images")
                .child(Objects.requireNonNull(selectedImageUri.getLastPathSegment()));

        AlertDialog progressDialog = showProgressDialog();

        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            imageURL = task.getResult().toString();
                            saveDataToDatabase();
                        }
                        progressDialog.dismiss();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Upload_Data.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDataToDatabase() {
        String title = editTextTopic.getText().toString().trim();
        String desc = editTextDesc.getText().toString().trim();
        String lang = editTextLang.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || lang.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        DataHelperClass dataClass = new DataHelperClass(title, desc, lang, imageURL);

        databaseReference.child(currentDate).setValue(dataClass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Upload_Data.this, "Data Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(Upload_Data.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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
