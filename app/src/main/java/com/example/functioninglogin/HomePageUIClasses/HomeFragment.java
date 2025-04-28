package com.example.functioninglogin.HomePageUIClasses;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftList;
import com.example.functioninglogin.HomePageUIClasses.ListManagment.ListViewFragment;
import com.example.functioninglogin.HomePageUIClasses.ListManagment.MyAdapter;
import com.example.functioninglogin.HomePageUIClasses.ListManagment.UploadListFragment;
import com.example.functioninglogin.HomePageUIClasses.MemberManagment.MemberDataClass;
import com.example.functioninglogin.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private View progressOverlay;
    private List<GiftList> dataList;
    private MyAdapter adapter;
    private DatabaseReference databaseReference;

    private boolean shouldRefreshOnResume = false;
    private boolean isFetching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        MaterialButton addListButton = view.findViewById(R.id.addListButton);
        SearchView searchView = view.findViewById(R.id.search);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        progressOverlay = view.findViewById(R.id.progressOverlay);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        dataList = new ArrayList<>();

        adapter = new MyAdapter(requireContext(), dataList, item -> {
            ListViewFragment fragment = ListViewFragment.newInstance(
                    item.getListId(),
                    item.getListTitle(),
                    String.valueOf(item.getTotalBudget()),
                    item.getListImage()
            );

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(userId);

        addListButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_fragment_container, new UploadListFragment())
                .addToBackStack(null)
                .commit());

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

        setupSwipeToDelete();
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
        if (isFetching) {
            Log.d("HOME_FRAGMENT", "fetchDataFromFirebase: Already fetching, skipping...");
            return;
        }
        isFetching = true;

        if (progressOverlay != null) progressOverlay.setVisibility(View.VISIBLE);

        databaseReference.child("lists").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot listSnap : snapshot.getChildren()) {
                    GiftList list = listSnap.getValue(GiftList.class);
                    if (list != null) {
                        list.setListId(listSnap.getKey());

                        if (listSnap.hasChild("members")) {
                            HashMap<String, MemberDataClass> members = new HashMap<>();
                            for (DataSnapshot mSnap : listSnap.child("members").getChildren()) {
                                MemberDataClass member = mSnap.getValue(MemberDataClass.class);
                                if (member != null) {
                                    member.setKey(mSnap.getKey());
                                    members.put(mSnap.getKey(), member);
                                }
                            }
                            list.setMembers(members);
                        }

                        dataList.add(list);
                    }
                }

                adapter.updateData(dataList);
                emptyTextView.setVisibility(dataList.isEmpty() ? View.VISIBLE : View.GONE);
                if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
                isFetching = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Live update error ❌", Toast.LENGTH_SHORT).show();
                if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
                isFetching = false;
            }
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

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position < 0 || position >= dataList.size()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                GiftList toDelete = dataList.get(position);

                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Delete List?")
                        .setMessage("Are you sure you want to delete the list \"" + toDelete.getListTitle() + "\" and all its members?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            String listId = toDelete.getListId();
                            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                            DatabaseReference listRef = FirebaseDatabase.getInstance()
                                    .getReference("Unique User ID")
                                    .child(userId)
                                    .child("lists")
                                    .child(listId);

                            listRef.removeValue()
                                    .addOnSuccessListener(unused -> {
                                        // Fetch the updated data from Firebase to ensure UI matches
                                        fetchDataFromFirebase();
                                        Toast.makeText(requireContext(), "List deleted 🎄", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Fetch the updated data to ensure UI matches Firebase state
                                        fetchDataFromFirebase();
                                        Toast.makeText(requireContext(), "Failed to delete ❌", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> adapter.notifyItemChanged(position))
                        .setCancelable(false)
                        .show();
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }
}