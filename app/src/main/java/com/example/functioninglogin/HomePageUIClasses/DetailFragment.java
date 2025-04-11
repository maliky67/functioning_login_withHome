package com.example.functioninglogin.HomePageUIClasses;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;

public class DetailFragment extends Fragment {

    private TextView detailTitle, detailPreferences, detailGiftIdea, detailPrice;
    private ImageView detailImage;
    private FloatingActionButton deleteButton, editButton;

    private String key = "", imageUrl = "", type = "", listKey = "";
    private DatabaseReference memberRef;

    public static DetailFragment newInstance(String key, String listKey, String type, String imageUrl) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("Key", key);
        args.putString("listKey", listKey);
        args.putString("type", type);
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        detailTitle = view.findViewById(R.id.detailTitle);
        detailPreferences = view.findViewById(R.id.detailPreferences);
        detailGiftIdea = view.findViewById(R.id.detailGiftIdea);
        detailPrice = view.findViewById(R.id.detailPrice);
        detailImage = view.findViewById(R.id.detailImage);
        deleteButton = view.findViewById(R.id.deleteButton);
        editButton = view.findViewById(R.id.editButton);

        if (getArguments() != null) {
            key = getArguments().getString("Key");
            listKey = getArguments().getString("listKey");
            type = getArguments().getString("type", "member");
            imageUrl = getArguments().getString("imageUrl");
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(uid)
                .child(listKey)
                .child("members")
                .child(key);

        loadMemberDetails();

        deleteButton.setOnClickListener(v -> deleteEntry());
        editButton.setOnClickListener(v -> openEditFragment());

        return view;
    }

    private void loadMemberDetails() {
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "Data not found", Toast.LENGTH_SHORT).show();
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

                if (isAdded()) {
                    Glide.with(requireContext()).load(imageUrl).into(detailImage);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteEntry() {
        memberRef.removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
        });

        if (imageUrl != null && !imageUrl.isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete();
        }
    }

    private void openEditFragment() {
        UpdateFragment fragment = UpdateFragment.newInstance(key, listKey, type, imageUrl);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
