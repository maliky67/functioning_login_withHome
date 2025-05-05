package com.example.functioninglogin.NavDrawerUIClasses;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

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
    private TextView previewText, previewDataText;

    private final List<String> listNames = new ArrayList<>();
    private final List<String> listKeys = new ArrayList<>();
    private final List<String> listBudgets = new ArrayList<>();
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
        previewDataText = view.findViewById(R.id.previewData);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Unique User ID")
                .child(uid)
                .child("lists");

        fetchListsFromDatabase();

        shareButton.setOnClickListener(v -> {
            int position = listSpinner.getSelectedItemPosition();
            if (position >= 0 && position < listNames.size()) {
                fetchMemberDataAndShare(listKeys.get(position), listNames.get(position), listBudgets.get(position));
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
                listKeys.clear();
                listBudgets.clear();

                for (DataSnapshot listSnapshot : snapshot.getChildren()) {
                    String title = listSnapshot.child("listTitle").getValue(String.class);
                    Object budgetObj = listSnapshot.child("totalBudget").getValue();
                    double budgetVal = (budgetObj != null) ? Double.parseDouble(budgetObj.toString()) : 0.0;
                    String budget = String.format("%.2f", budgetVal);

                    listNames.add(title != null ? title : "Unnamed List");
                    listKeys.add(listSnapshot.getKey());
                    listBudgets.add(budget);
                }

                setupSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading lists ❌", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMemberDataAndShare(String listId, String listTitle, String budget) {
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
                                String imageUrl = giftSnap.child("imageUrl").getValue(String.class);

                                GiftItem item = new GiftItem(giftName, notes, price, status, website, imageUrl);
                                member.addGift(item);
                            }

                            giftMemberList.add(member);
                        }

                        Uri pdfUri = PDFUtil.createDetailedListPdf(requireContext(), listTitle, budget, giftMemberList);

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

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String listId = listKeys.get(position);
                String listTitle = listNames.get(position);
                String listBudget = listBudgets.get(position);

                previewDataText.setText("🔄 Loading preview...");

                databaseReference.child(listId).child("members")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                giftMemberList.clear();

                                StringBuilder previewBuilder = new StringBuilder();
                                previewBuilder.append("📄 ").append(listTitle).append("\n");
                                previewBuilder.append("💰 Total Budget: $").append(listBudget).append("\n\n");

                                for (DataSnapshot memberSnap : snapshot.getChildren()) {
                                    String name = memberSnap.child("name").getValue(String.class);
                                    String role = memberSnap.child("role").getValue(String.class);

                                    previewBuilder.append("👤 ").append(name != null ? name : "Unknown")
                                            .append(" (").append(role != null ? role : "").append(")\n");

                                    GiftMember member = new GiftMember(name, role);

                                    for (DataSnapshot giftSnap : memberSnap.child("gifts").getChildren()) {
                                        String giftName = giftSnap.child("name").getValue(String.class);
                                        String price = giftSnap.child("price").getValue(String.class);
                                        String status = giftSnap.child("status").getValue(String.class);
                                        String website = giftSnap.child("website").getValue(String.class);
                                        String notes = giftSnap.child("notes").getValue(String.class);
                                        String imageUrl = giftSnap.child("imageUrl").getValue(String.class);

                                        // 🔁 Map status emoji
                                        String emoji = "💡";
                                        if (status != null) {
                                            switch (status.toLowerCase()) {
                                                case "bought": emoji = "💸"; break;
                                                case "arrived": emoji = "📦"; break;
                                                case "wrapped": emoji = "🎁"; break;
                                            }
                                        }

                                        previewBuilder.append("   ").append(emoji).append(" ").append(giftName);
                                        if (status != null) previewBuilder.append(" - ").append(status);
                                        if (price != null && !price.isEmpty())
                                            previewBuilder.append(" 💵 $").append(price);
                                        previewBuilder.append("\n");

                                        GiftItem item = new GiftItem(giftName, notes, price, status, website, imageUrl);
                                        member.addGift(item);
                                    }

                                    giftMemberList.add(member);
                                    previewBuilder.append("\n");
                                }

                                previewDataText.setText(previewBuilder.toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                previewDataText.setText("⚠️ Failed to load preview.");
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                previewDataText.setText("Select a list to see a preview.");
            }
        });
    }
}
