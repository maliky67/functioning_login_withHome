package com.example.functioninglogin.NavDrawerUIClasses;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftMember;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ShareFragment extends Fragment {

    private Spinner listSpinner;
    private TextView previewText;

    private final List<String> listNames = new ArrayList<>();
    private final List<String> listDescriptions = new ArrayList<>();
    private final List<String> listKeys = new ArrayList<>();
    private final List<GiftMember> giftMemberList = new ArrayList<>();

    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.share_fragment, container, false);

        listSpinner = view.findViewById(R.id.list_spinner);
        Button shareButton = view.findViewById(R.id.share_button);
        previewText = view.findViewById(R.id.preview_text);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(uid)
                .child("lists");

        fetchListsFromDatabase();

        shareButton.setOnClickListener(v -> {
            int position = listSpinner.getSelectedItemPosition();
            if (position >= 0 && position < listNames.size()) {
                fetchMemberDataAndShare(listKeys.get(position));
            } else {
                Toast.makeText(getContext(), "Please select a list", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchListsFromDatabase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listNames.clear();
                listDescriptions.clear();
                listKeys.clear();

                for (DataSnapshot listSnapshot : snapshot.getChildren()) {
                    String title = listSnapshot.child("listTitle").getValue(String.class);
                    String desc = listSnapshot.child("listDesc").getValue(String.class);

                    listNames.add(title != null ? title : "Unnamed List");
                    listDescriptions.add(desc != null ? desc : "(No description)");
                    listKeys.add(listSnapshot.getKey());
                }

                setupSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading lists ❌", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMemberDataAndShare(String listId) {
        giftMemberList.clear();
        databaseReference.child(listId).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot memberSnap : snapshot.getChildren()) {
                            String name = memberSnap.child("name").getValue(String.class);
                            String role = memberSnap.child("role").getValue(String.class);

                            GiftMember member = new GiftMember(name, role);

                            for (DataSnapshot giftSnap : memberSnap.child("gifts").getChildren()) {
                                String giftName = giftSnap.child("name").getValue(String.class);
                                String notes = giftSnap.child("notes").getValue(String.class);
                                String price = giftSnap.child("price").getValue(String.class);
                                String status = giftSnap.child("status").getValue(String.class);
                                String website = giftSnap.child("website").getValue(String.class);

                                GiftItem item = new GiftItem(giftName, notes, price, status, website);
                                member.addGift(item);
                            }

                            giftMemberList.add(member);
                        }

                        // Call PDF Util
                        String title = listNames.get(listSpinner.getSelectedItemPosition());
                        String desc = listDescriptions.get(listSpinner.getSelectedItemPosition());

                        Uri pdfUri = PDFUtil.createDetailedListPdf(requireContext(), title, desc, giftMemberList);

                        if (pdfUri != null) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/pdf");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
                        } else {
                            Toast.makeText(getContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load member data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listSpinner.setAdapter(adapter);

        listSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String preview = "🎁 " + listNames.get(position) + ":\n" + listDescriptions.get(position);
                previewText.setText(preview);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                previewText.setText("");
            }
        });
    }
}
