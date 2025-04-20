package com.example.functioninglogin.HomePageUIClasses.ListManagment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.*;

import java.util.Objects;

public class EditListFragment extends Fragment {

    private ImageView editImage;
    private EditText editListName, editTotalBudget;
    private Button editButton;
    private Uri selectedImageUri;
    private String imageUrl;
    private String listKey;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static EditListFragment newInstance(String key, String title, String budget, String imageUrl) {
        EditListFragment fragment = new EditListFragment();
        Bundle args = new Bundle();
        args.putString("Key", key);
        args.putString("Title", title);
        args.putString("Budget", budget);
        args.putString("Image", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_list, container, false);

        editImage = view.findViewById(R.id.editImage);
        editListName = view.findViewById(R.id.editListName);
        editTotalBudget = view.findViewById(R.id.editTotalBudget);
        editButton = view.findViewById(R.id.EditButton);

        if (getArguments() != null) {
            listKey = getArguments().getString("Key", "");
            editListName.setText(getArguments().getString("Title", ""));
            editTotalBudget.setText(getArguments().getString("Budget", ""));
            imageUrl = getArguments().getString("Image");

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext()).load(imageUrl).into(editImage);
            }
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        editImage.setImageURI(selectedImageUri);
                    }
                }
        );

        editImage.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            imagePickerLauncher.launch(pickImage);
        });

        editButton.setOnClickListener(v -> {
            String name = editListName.getText().toString().trim();
            String budgetStr = editTotalBudget.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Name can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            double budget = 0;
            if (!budgetStr.isEmpty()) {
                try {
                    budget = Double.parseDouble(budgetStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid budget", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (selectedImageUri != null) {
                uploadImageAndSave(name, budget);
            } else {
                updateFirebase(name, budget, imageUrl);
            }
        });

        return view;
    }

    private void uploadImageAndSave(String name, double budget) {
        AlertDialog dialog = showProgressDialog();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("Android Images")
                .child(Objects.requireNonNull(selectedImageUri.getLastPathSegment()));

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                dialog.dismiss();
                                updateFirebase(name, budget, uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFirebase(String name, double budget, String newImageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(listKey);

        ref.child("listTitle").setValue(name);
        ref.child("totalBudget").setValue(budget);
        ref.child("listImage").setValue(newImageUrl);

        Toast.makeText(requireContext(), "List updated", Toast.LENGTH_SHORT).show();
        Bundle result = new Bundle();
        result.putBoolean("refreshListHeader", true);
        getParentFragmentManager().setFragmentResult("refreshList", result);
        requireActivity().getSupportFragmentManager().popBackStack(); // return to ListView

    }

    private AlertDialog showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(R.layout.progress_layout);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
