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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ListViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab, deleteFab;
    private List<MemberDataClass> memberList;
    private MemberAdapter memberAdapter;
    private DatabaseReference memberRef;
    private String listKey;

    private TextView headerTitle, headerDesc;
    private ImageView headerImage;

    public ListViewFragment() {}

    public static ListViewFragment newInstance(String key, String title, String desc, String imageUrl) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString("Key", key);
        args.putString("Title", title);
        args.putString("Description", desc);
        args.putString("Image", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

        headerTitle = view.findViewById(R.id.headerTitle);
        headerDesc = view.findViewById(R.id.headerDesc);
        headerImage = view.findViewById(R.id.headerImage);
        recyclerView = view.findViewById(R.id.ListrecyclerView);
        fab = view.findViewById(R.id.memberfab);
        deleteFab = view.findViewById(R.id.memberdeletefab);

        Bundle bundle = getArguments();
        if (bundle != null) {
            listKey = bundle.getString("Key");
            headerTitle.setText(bundle.getString("Title", "List Title"));
            headerDesc.setText(bundle.getString("Description", "Description"));
            Glide.with(requireContext()).load(bundle.getString("Image")).into(headerImage);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(requireContext(), memberList, listKey);
        recyclerView.setAdapter(memberAdapter);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        memberRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child(listKey)
                .child("members");

        memberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberList.clear();
                for (DataSnapshot memberSnap : snapshot.getChildren()) {
                    MemberDataClass member = memberSnap.getValue(MemberDataClass.class);
                    if (member != null) {
                        member.setKey(memberSnap.getKey());
                        memberList.add(member);
                    }
                }
                memberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load members.", Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnClickListener(v -> {
            MemberDataUploadFragment fragment = MemberDataUploadFragment.newInstance(listKey);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        deleteFab.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Entire List?")
                    .setMessage("This will permanently delete the list and all its members. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEntireList())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void deleteEntireList() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference listRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child(listKey);

        listRef.removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "List deleted successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
