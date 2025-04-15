package com.example.functioninglogin.LoginUIClasses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupFragment extends Fragment {

    private EditText signupName, signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    public SignupFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        signupName = view.findViewById(R.id.signup_name);
        signupEmail = view.findViewById(R.id.signup_email);
        signupPassword = view.findViewById(R.id.signup_password);
        signupButton = view.findViewById(R.id.signup_button);
        loginRedirectText = view.findViewById(R.id.loginRedirectText);

        auth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(v -> {
            String name = signupName.getText().toString();
            String email = signupEmail.getText().toString();
            String password = signupPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            String userId = user.getUid();

                            databaseReference = FirebaseDatabase.getInstance()
                                    .getReference("users");

                            HelperClass helperClass = new HelperClass(userId, name, email);
                            databaseReference.child(userId).setValue(helperClass);

                            Toast.makeText(requireContext(), "Signup successful!", Toast.LENGTH_SHORT).show();

                            // Go back to login fragment
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loginRedirectText.setOnClickListener(v -> {
            // Navigate back to LoginFragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}
