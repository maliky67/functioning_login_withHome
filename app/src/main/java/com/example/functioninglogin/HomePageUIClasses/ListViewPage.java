package com.example.functioninglogin.HomePageUIClasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListViewPage extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab, deleteFab;
    List<MemberDataClass> memberList;
    MemberAdapter memberAdapter;
    DatabaseReference memberRef;
    String listKey, userId;

    // Header views
    TextView headerTitle, headerDesc;
    ImageView headerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_page);

        // Get passed list info
        listKey = getIntent().getStringExtra("Key");
        String listTitle = getIntent().getStringExtra("Title");
        String listDesc = getIntent().getStringExtra("Description");
        String listImage = getIntent().getStringExtra("Image");

        // Setup header view
        headerTitle = findViewById(R.id.headerTitle);
        headerDesc = findViewById(R.id.headerDesc);
        headerImage = findViewById(R.id.headerImage);

        headerTitle.setText(listTitle != null ? listTitle : "List Title");
        headerDesc.setText(listDesc != null ? listDesc : "Description");
        Glide.with(this).load(listImage).into(headerImage);

        // Setup member list
        recyclerView = findViewById(R.id.ListrecyclerView);
        fab = findViewById(R.id.memberfab);
        deleteFab = findViewById(R.id.memberdeletefab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(this, memberList, listKey);
        recyclerView.setAdapter(memberAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                Toast.makeText(ListViewPage.this, "Failed to load members.", Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnClickListener(v -> {
            Intent i = new Intent(ListViewPage.this, Member_Data_Upload.class);
            i.putExtra("listKey", listKey);
            startActivity(i);
        });

        deleteFab.setOnClickListener(v -> {
            new AlertDialog.Builder(ListViewPage.this)
                    .setTitle("Delete Entire List?")
                    .setMessage("This will permanently delete the list and all its members. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEntireList())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteEntireList() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference listRef = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId)
                .child(listKey);

        listRef.removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "List deleted successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeFragment.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
