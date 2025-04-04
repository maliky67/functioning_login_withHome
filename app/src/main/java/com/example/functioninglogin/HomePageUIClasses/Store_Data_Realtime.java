package com.example.functioninglogin.HomePageUIClasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class Store_Data_Realtime extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView recyclerView;
    SearchView searchView;

    List<DataHelperClass> dataList;
    MyAdapter adapter;

    DatabaseReference databaseReference;
    ValueEventListener eventListener;

    Map<String, DataHelperClass> previousDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_data_realtime);

        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        dataList = new ArrayList<>();
        adapter = new MyAdapter(this, dataList);
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = showProgressDialog();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Unique User ID").child(userId);

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();

                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    DataHelperClass data = listSnap.getValue(DataHelperClass.class);
                    if (data != null) {
                        data.setKey(listSnap.getKey());

                        DataHelperClass oldData = previousDataMap.get(data.getKey());
                        if (oldData == null || !oldData.equals(data)) {
                            previousDataMap.put(data.getKey(), data); // track for change detection
                        }

                        dataList.add(data);
                    }
                }

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Store_Data_Realtime.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, Upload_Data.class);
            startActivity(intent);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });
    }

    private void searchList(String text) {
        if (text.isEmpty()) {
            adapter.searchDataList(new ArrayList<>(dataList));
        } else {
            ArrayList<DataHelperClass> filtered = new ArrayList<>();
            for (DataHelperClass d : dataList) {
                if (d.getDataTitle().toLowerCase().contains(text.toLowerCase())) {
                    filtered.add(d);
                }
            }
            adapter.searchDataList(filtered);
        }
    }

    private AlertDialog showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
