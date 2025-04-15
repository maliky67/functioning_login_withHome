package com.example.functioninglogin.LoginUIClasses;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.HomePageUIClasses.MainActivity;
import com.example.functioninglogin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText, forgotpasswordRedirectText;
    private FirebaseAuth auth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginEmail = view.findViewById(R.id.login_username);
        loginPassword = view.findViewById(R.id.login_password);
        signupRedirectText = view.findViewById(R.id.loginRedirectText);
        forgotpasswordRedirectText = view.findViewById(R.id.forgot_passwordRedirectText);
        loginButton = view.findViewById(R.id.login_button);

        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();

                                Intent intent = new Intent(requireContext(), MainActivity.class);
                                intent.putExtra("userId", userId);
                                startActivity(intent);
                                requireActivity().finish();
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "Login Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        signupRedirectText.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new SignupFragment())
                    .addToBackStack(null)
                    .commit();
        });

        forgotpasswordRedirectText.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
