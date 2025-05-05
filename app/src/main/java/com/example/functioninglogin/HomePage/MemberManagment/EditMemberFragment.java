package com.example.functioninglogin.HomePage.MemberManagment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Objects;

public class EditMemberFragment extends Fragment {

    private ImageView memberImageView;
    private EditText nameEditText, roleEditText;
    private DatabaseReference memberRef;
    private ProgressDialog progressDialog;

    public static EditMemberFragment newInstance(String listKey, String memberKey) {
        EditMemberFragment fragment = new EditMemberFragment();
        Bundle args = new Bundle();
        args.putString("listKey", listKey);
        args.putString("memberKey", memberKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_member, container, false);

        // 💡 Firebase paths
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        assert getArguments() != null;
        String listKey = getArguments().getString("listKey");
        String memberKey = getArguments().getString("memberKey");
        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child("lists")
                .child(Objects.requireNonNull(listKey))
                .child("members")
                .child(Objects.requireNonNull(memberKey));

        // 🧱 Bind Views
        memberImageView = view.findViewById(R.id.editmemberImageView);
        nameEditText = view.findViewById(R.id.editmemberNameEditText);
        roleEditText = view.findViewById(R.id.editmemberRoleEditText);
        Button saveButton = view.findViewById(R.id.editMemberButton);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);

        // 🧠 Load existing member info
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MemberDataClass member = snapshot.getValue(MemberDataClass.class);
                if (member != null) {
                    nameEditText.setText(member.getName());
                    roleEditText.setText(member.getRole());

                    if (member.getImageUrl() != null && !member.getImageUrl().isEmpty()) {
                        Glide.with(requireContext()).load(member.getImageUrl()).into(memberImageView);
                    } else {
                        memberImageView.setImageResource(R.drawable.baseline_account_box_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "❌ Failed to load member", Toast.LENGTH_SHORT).show();
            }
        });

        // 🖊️ Save changes
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newRole = roleEditText.getText().toString().trim();

            if (newName.isEmpty()) {
                nameEditText.setError("Name required");
                return;
            }

            progressDialog.show();

            memberRef.child("name").setValue(newName);
            memberRef.child("role").setValue(newRole)
                    .addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "✅ Member updated", Toast.LENGTH_SHORT).show();

                        requireActivity().getSupportFragmentManager().popBackStack(); // or trigger refresh
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "❌ Failed to save", Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }
}
