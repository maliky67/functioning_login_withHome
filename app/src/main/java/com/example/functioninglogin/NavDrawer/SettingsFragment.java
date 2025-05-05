package com.example.functioninglogin.NavDrawer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.functioninglogin.LoginUI.AuthActivity;
import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends PreferenceFragmentCompat {

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);
        context = getContext();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        setupEmailPreference();
        setupChangePasswordPreference();
        setupDarkModePreference();
        setupLanguagePreference();
        setupLogoutPreference();
    }



    private void setupEmailPreference() {
        EditTextPreference emailPref = findPreference("email");
        if (emailPref != null && currentUser != null) {
            if (currentUser.getEmail() != null) {
                emailPref.setText(currentUser.getEmail());
            }

            emailPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String newEmail = newValue.toString().trim();
                if (!newEmail.isEmpty()) {
                    reAuthenticateAndUpdateEmail(newEmail);
                }
                return true;
            });
        }
    }

    private void setupChangePasswordPreference() {
        Preference changePassPref = findPreference("change_password");
        if (changePassPref != null) {
            changePassPref.setOnPreferenceClickListener(preference -> {
                if (currentUser != null) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(currentUser.getEmail())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Password reset email sent ✅", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to send reset email ❌", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                return true;
            });
        }
    }

    private void setupDarkModePreference() {
        SwitchPreferenceCompat darkModePref = findPreference("dark_mode");
        if (darkModePref != null) {
            darkModePref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isDarkMode = (Boolean) newValue;
                AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                        AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            });
        }
    }

    private void setupLogoutPreference() {
        Preference logoutPref = findPreference("logout");
        if (logoutPref != null) {
            logoutPref.setOnPreferenceClickListener(preference -> {
                firebaseAuth.signOut();
                Intent intent = new Intent(getActivity(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            });
        }
    }


    private void reAuthenticateAndUpdateEmail(String newEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Re-authentication Required");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter your password");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String password = input.getText().toString().trim();

            if (currentUser.getEmail() != null) {
                firebaseAuth.signInWithEmailAndPassword(currentUser.getEmail(), password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateEmail(newEmail);
                            } else {
                                Toast.makeText(context, "Re-authentication failed ❌", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateEmail(String newEmail) {
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Email updated ✅", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update email ❌", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupLanguagePreference() {
        ListPreference languagePref = findPreference("language");
        if (languagePref != null) {
            languagePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String langCode = newValue.toString();
                LocaleHelper.updateResources(requireContext(), langCode);
                restartApp();
                return true;
            });
        }
    }

    private void restartApp() {
        Intent intent = new Intent(requireActivity(), getActivity().getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

}
