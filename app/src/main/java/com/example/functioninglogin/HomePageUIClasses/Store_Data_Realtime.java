package com.example.functioninglogin.HomePageUIClasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


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

public class Store_Data_Realtime extends AppCompatActivity {
    FloatingActionButton fab;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<DataHelperClass> dataList;
    MyAdapter adapter;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_data_realtime);

        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(Store_Data_Realtime.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(Store_Data_Realtime.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        adapter = new MyAdapter(Store_Data_Realtime.this, dataList);
        recyclerView.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "NULL";

        Log.d("FIREBASE_DEBUG", "User ID: " + userId);

        if (userId.equals("NULL")) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_LONG).show();
            dialog.dismiss(); // Dismiss the progress dialog
            return; // Exit function
        }

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Android Tutorials")
                .child(userId); // Fetch only this user's data

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                if (!snapshot.exists()) {
                    Log.d("FIREBASE_DEBUG", "No data found for user.");
                    Toast.makeText(Store_Data_Realtime.this, "No data found", Toast.LENGTH_LONG).show();
                } else {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        DataHelperClass dataClass = itemSnapshot.getValue(DataHelperClass.class);
                        assert dataClass != null;
                        dataClass.setKey(itemSnapshot.getKey());
                        dataList.add(dataClass);
                    }
                    adapter.notifyDataSetChanged();
                }
                dialog.dismiss(); // Ensure dialog is dismissed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(Store_Data_Realtime.this, Upload_Data.class);
            startActivity(intent);
        });

    }
    public void searchList(String text) {
        if (text.isEmpty()) {
            adapter.searchDataList(new ArrayList<>(dataList));
        } else {
            ArrayList<DataHelperClass> filteredList = new ArrayList<>();
            for (DataHelperClass data : dataList) {
                if (data.getDataTitle().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(data);
                }
            }
            adapter.searchDataList(filteredList);
        }
    }}
