package com.example.functioninglogin.NavDrawer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.functioninglogin.R;

public class AboutUsActivity extends AppCompatActivity {

    private int[] photoIds = {
            R.drawable.joytechv1, R.drawable.joytechv1, R.drawable.joytechv1,
            R.drawable.joytechv1, R.drawable.joytechv1, R.drawable.joytechv1
    };

    private String[] names;
    private String[] roles;
    private String[] descriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Load string arrays from resources
        names = new String[] {
                getString(R.string.name_derek),
                getString(R.string.name_joshua),
                getString(R.string.name_jose),
                getString(R.string.name_adrian),
                getString(R.string.name_fernando),
                getString(R.string.name_jose_mojica)
        };

        roles = new String[] {
                getString(R.string.role_derek),
                getString(R.string.role_joshua),
                getString(R.string.role_jose),
                getString(R.string.role_adrian),
                getString(R.string.role_fernando),
                getString(R.string.role_jose_mojica)
        };

        descriptions = new String[] {
                getString(R.string.desc_derek),
                getString(R.string.desc_joshua),
                getString(R.string.desc_jose),
                getString(R.string.desc_adrian),
                getString(R.string.desc_fernando),
                getString(R.string.desc_jose_mojica)
        };

        Toolbar toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        LinearLayout container = findViewById(R.id.member_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < names.length; i++) {
            View card = inflater.inflate(R.layout.card_about_member, container, false);

            ImageView photo = card.findViewById(R.id.member_photo);
            TextView name = card.findViewById(R.id.member_name);
            TextView role = card.findViewById(R.id.member_role);

            photo.setImageResource(photoIds[i]);
            name.setText(names[i]);
            role.setText(getString(R.string.role_prefix) + roles[i]);

            int finalI = i;
            card.setOnClickListener(v -> showMemberDialog(names[finalI], roles[finalI], descriptions[finalI], photoIds[finalI]));

            container.addView(card);
        }
    }

    private void showMemberDialog(String name, String role, String description, int imageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_member_info, null);

        TextView nameText = dialogView.findViewById(R.id.dialog_member_name);
        TextView roleText = dialogView.findViewById(R.id.dialog_member_role);
        TextView descText = dialogView.findViewById(R.id.dialog_member_description);
        ImageView photoView = dialogView.findViewById(R.id.dialog_member_photo);

        nameText.setText(name);
        roleText.setText(role);
        descText.setText(description);
        photoView.setImageResource(imageResId);

        builder.setView(dialogView)
                .setPositiveButton(getString(R.string.close), null)
                .create()
                .show();
    }
}
