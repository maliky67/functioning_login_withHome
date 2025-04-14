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

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private SearchView searchView;
    private List<GiftList> dataList;
    private MyAdapter adapter;
    private DatabaseReference databaseReference;
    private AlertDialog dialog;

    private boolean shouldRefreshOnResume = false;
    private boolean isFetching = false;

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
                    item.getListId(),           // ✅ Firebase key
                    item.getListTitle(),
                    item.getListDesc(),
                    item.getListImage()
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

        fab.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_fragment_container, new UploadListFragment())
                .addToBackStack(null)
                .commit()
        );

        getParentFragmentManager().setFragmentResultListener("refreshHome", this, (requestKey, bundle) -> {
            boolean refresh = bundle.getBoolean("refreshNeeded", false);
            Log.d("FRAGMENT_RESULT", "Triggered with refresh: " + refresh);
            if (refresh) {
                shouldRefreshOnResume = true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

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

    private void fetchDataFromFirebase() {
        if (isFetching) return;
        isFetching = true;

        if (dialog == null) {
            dialog = new AlertDialog.Builder(requireContext())
                    .setView(R.layout.progress_layout)
                    .setCancelable(false)
                    .create();
        }

        dialog.show();
        dataList.clear();

        databaseReference.child("lists").get().addOnSuccessListener(snapshot -> {
            Log.d("FIREBASE_DATA", "Fetched: " + snapshot.getChildrenCount());

            for (DataSnapshot listSnap : snapshot.getChildren()) {
                GiftList list = listSnap.getValue(GiftList.class);
                if (list != null) {
                    list.setListId(listSnap.getKey()); // ✅ Store Firebase key
                    dataList.add(list);
                }
            }

            adapter.updateData(dataList);
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
        ArrayList<GiftList> filtered = new ArrayList<>();
        for (GiftList d : dataList) {
            if (d.getListTitle().toLowerCase().contains(text.toLowerCase())) {
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
