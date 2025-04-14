package com.example.functioninglogin.HomePageUIClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class UploadListFragment extends Fragment {

    private ImageView imageViewUpload;
    private EditText editTextTopic, editTextDesc;
    private Button buttonSave;

    private Uri selectedImageUri;
    private String imageURL;

    private DatabaseReference databaseReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public UploadListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_list, container, false);

        imageViewUpload = view.findViewById(R.id.uploadImage);
        editTextTopic = view.findViewById(R.id.uploadTopic);
        editTextDesc = view.findViewById(R.id.uploadDesc);
        buttonSave = view.findViewById(R.id.saveButton);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageViewUpload.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        imageViewUpload.setOnClickListener(v -> openImagePicker());

        buttonSave.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageToFirebase();
            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
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
                            Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDataToDatabase(AlertDialog dialog) {
        String title = editTextTopic.getText().toString().trim();
        String desc = editTextDesc.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            dialog.dismiss();
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique list ID
        String listId = databaseReference.child("lists").push().getKey();

        // Create GiftList object
        GiftList giftList = new GiftList(
                title,
                desc,
                imageURL,
                System.currentTimeMillis()
        );

        // Save GiftList object under /users/userId/lists/listId
        databaseReference
                .child("lists")
                .child(listId)
                .setValue(giftList)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Upload Successful!", Toast.LENGTH_SHORT).show();

                    // Notify home to refresh list
                    Bundle result = new Bundle();
                    result.putBoolean("refreshNeeded", true);
                    requireActivity().getSupportFragmentManager().setFragmentResult("refreshHome", result);
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
