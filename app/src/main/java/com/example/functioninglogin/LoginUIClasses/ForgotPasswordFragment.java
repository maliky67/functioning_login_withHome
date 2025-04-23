package com.example.functioninglogin.LoginUIClasses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordFragment extends Fragment {

    private EditText forgotEmail;
    private FirebaseAuth auth;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forget_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        forgotEmail = view.findViewById(R.id.forgot_email);
        Button resetButton = view.findViewById(R.id.btn_reset_password);
        auth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(v -> {
            String email = forgotEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Reset link sent to your email.", Toast.LENGTH_LONG).show();

                            // Optional: navigate back to login
                            requireActivity().getSupportFragmentManager().popBackStack();

                        } else {
                            Toast.makeText(requireContext(),
                                    "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
