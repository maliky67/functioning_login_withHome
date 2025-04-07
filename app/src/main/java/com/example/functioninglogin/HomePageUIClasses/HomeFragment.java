package com.example.functioninglogin.HomePageUIClasses;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private SearchView searchView;
    private List<DataHelperClass> dataList;
    private MyAdapter adapter;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;
    private AlertDialog dialog;
    private boolean isDialogShown = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false); // ← Use your FrameLayout layout here

        recyclerView = view.findViewById(R.id.recyclerView);
        fab = view.findViewById(R.id.fab);
        searchView = view.findViewById(R.id.search); // ✅ Correctly scoped

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        dataList = new ArrayList<>();
        adapter = new MyAdapter(requireContext(), dataList);
        recyclerView.setAdapter(adapter);

        // ✅ Show dialog once
        if (!isDialogShown) {
            dialog = new AlertDialog.Builder(requireContext())
                    .setView(R.layout.progress_layout)
                    .setCancelable(false)
                    .create();
            dialog.show();
            isDialogShown = true;
        }

        // 🔗 Firebase Setup
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Unique User ID").child(userId);

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();

                Log.d("FIREBASE_DATA", "Fetched: " + snapshot.getChildrenCount());

                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    Object rawData = listSnap.getValue();
                    DataHelperClass data = null;
                    if (rawData instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) rawData;
                        String title = (String) map.get("dataTitle");
                        String desc = (String) map.get("dataDesc");

                        // Handle both String and nested "dataImage"
                        Object imgObj = map.get("dataImage");
                        String imageUrl = null;
                        if (imgObj instanceof String) {
                            imageUrl = (String) imgObj;
                        } else if (imgObj instanceof Map) {
                            imageUrl = (String) ((Map<?, ?>) imgObj).get("url");
                        }

                        data = new DataHelperClass(title, desc, imageUrl);
                        data.setKey(listSnap.getKey());
                        dataList.add(data);
                    }
                }
                adapter.notifyDataSetChanged();

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    isDialogShown = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load data ❌", Toast.LENGTH_SHORT).show();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    isDialogShown = false;
                }
            }
        });

        // ➕ Add new data
        fab.setOnClickListener(v -> startActivity(new Intent(requireContext(), Upload_Data.class)));

        // 🔍 Search filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        return view;
    }

    private void searchList(String text) {
        ArrayList<DataHelperClass> filtered = new ArrayList<>();
        for (DataHelperClass d : dataList) {
            if (d.getDataTitle().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(d);
            }
        }
        adapter.searchDataList(filtered);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }

        // 🔄 Clean up dialog
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        isDialogShown = false;
    }
}
