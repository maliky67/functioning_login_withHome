package com.example.functioninglogin.HomePageUIClasses.ListManagment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftList;
import com.example.functioninglogin.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class UploadListFragment extends Fragment {

    private ImageView imageViewUpload;
    private EditText editTextTopic, editTextDesc;

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
        Button buttonSave = view.findViewById(R.id.saveButton);

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
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
                // No image selected — use local drawable
                selectedImageUri = getDrawableUri(R.drawable.baseline_ac_unit_24); // reuse the same upload logic
                uploadImageToFirebase();
            }
        });



        return view;
    }

    private Uri getDrawableUri(int drawableId) {
        try {
            Drawable drawable = AppCompatResources.getDrawable(requireContext(), drawableId);
            if (drawable == null) {
                Toast.makeText(requireContext(), "Drawable not found", Toast.LENGTH_SHORT).show();
                return null;
            }

            Bitmap bitmap;
            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            } else {
                // Convert vector drawable to bitmap
                int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 200;
                int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 200;
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }

            File file = new File(requireContext().getCacheDir(), "default_image.png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            return FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to use default image", Toast.LENGTH_SHORT).show();
            return null;
        }
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
        String budgetText = editTextDesc.getText().toString().trim(); // rename this var if you want

        if (title.isEmpty()) {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Please enter a list name", Toast.LENGTH_SHORT).show();
            return;
        }
        double budgetValue = 0;
        if (!budgetText.isEmpty()) {
            try {
                budgetValue = Double.parseDouble(budgetText);
            } catch (NumberFormatException e) {
                dialog.dismiss();
                Toast.makeText(requireContext(), "Budget must be a number", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Generate unique list ID
        String listId = databaseReference.child("lists").push().getKey();

        // Create GiftList object
        GiftList giftList = new GiftList(
                title,
                budgetValue,
                imageURL,
                System.currentTimeMillis()
        );

        // Save GiftList object under /users/userId/lists/listId
        databaseReference
                .child("lists")
                .child(Objects.requireNonNull(listId))
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
