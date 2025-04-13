package com.example.functioninglogin.HomePageUIClasses;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
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
    private AlertDialog dialog;

    private boolean shouldRefreshOnResume = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fab = view.findViewById(R.id.fab);
        searchView = view.findViewById(R.id.search);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        dataList = new ArrayList<>();

        adapter = new MyAdapter(requireContext(), dataList, item -> {
            ListViewFragment fragment = ListViewFragment.newInstance(
                    item.getKey(),
                    item.getDataTitle(),
                    item.getDataDesc(),
                    item.getDataImage()
            );

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId);

        // FAB ➕ opens UploadListFragment
        fab.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_fragment_container, new UploadListFragment())
                .addToBackStack(null)
                .commit()
        );

        // ✅ Refresh flag set by result
        getParentFragmentManager().setFragmentResultListener("refreshHome", this, (requestKey, bundle) -> {
            boolean refresh = bundle.getBoolean("refreshNeeded", false);
            Log.d("FRAGMENT_RESULT", "Triggered with refresh: " + refresh);
            if (refresh) {
                shouldRefreshOnResume = true;
            }
        });

        // Search bar filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        // Initial load
        fetchDataFromFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldRefreshOnResume || dataList.isEmpty()) {
            Log.d("HOME_FRAGMENT", "onResume: Fetching due to flag or empty list");
            shouldRefreshOnResume = false;
            fetchDataFromFirebase();
        } else {
            Log.d("HOME_FRAGMENT", "onResume: Skipping fetch (already populated)");
        }
    }

    private boolean isFetching = false;

    private void fetchDataFromFirebase() {
        if (isFetching) return; // 🚫 prevent duplicate calls
        isFetching = true;

        if (dialog == null) {
            dialog = new AlertDialog.Builder(requireContext())
                    .setView(R.layout.progress_layout)
                    .setCancelable(false)
                    .create();
        }

        dialog.show();
        dataList.clear();

        databaseReference.get().addOnSuccessListener(snapshot -> {
            Log.d("FIREBASE_DATA", "Fetched: " + snapshot.getChildrenCount());

            for (DataSnapshot listSnap : snapshot.getChildren()) {
                Object rawData = listSnap.getValue();
                if (rawData instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) rawData;
                    String title = (String) map.get("dataTitle");
                    String desc = (String) map.get("dataDesc");

                    Object imgObj = map.get("dataImage");
                    String imageUrl = null;
                    if (imgObj instanceof String) {
                        imageUrl = (String) imgObj;
                    } else if (imgObj instanceof Map) {
                        imageUrl = (String) ((Map<?, ?>) imgObj).get("url");
                    }

                    if (title != null && desc != null) {
                        DataHelperClass data = new DataHelperClass(title, desc, imageUrl);
                        data.setKey(listSnap.getKey());
                        dataList.add(data);
                    }
                }
            }

            adapter.updateData(dataList); // ✅ best practice to reset base list
            Log.d("DATA_LIST_SIZE", "After fetch: " + dataList.size());
            dialog.dismiss();
            isFetching = false;
        }).addOnFailureListener(error -> {
            dialog.dismiss();
            isFetching = false;
            Toast.makeText(requireContext(), "Failed to fetch data ❌", Toast.LENGTH_SHORT).show();
        });
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
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
