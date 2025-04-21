package com.example.functioninglogin.LoginUIClasses;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.HomePageUIClasses.MainActivity;
import com.example.functioninglogin.R;
import com.google.firebase.auth.*;

public class LoginFragment extends Fragment {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText, forgotPasswordRedirectText;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginEmail = view.findViewById(R.id.login_username);
        loginPassword = view.findViewById(R.id.login_password);
        passwordToggle = view.findViewById(R.id.password_toggle);
        loginButton = view.findViewById(R.id.login_button);
        signupRedirectText = view.findViewById(R.id.loginRedirectText);
        forgotPasswordRedirectText = view.findViewById(R.id.forgot_passwordRedirectText);

        auth = FirebaseAuth.getInstance();

        // 👁️ Toggle password visibility
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.blind_eye); // Swap this with your "eye closed" icon
            } else {
                // Show password
                loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.eye_open); // Swap this with your "eye open" icon
            }
            isPasswordVisible = !isPasswordVisible;
            loginPassword.setSelection(loginPassword.getText().length()); // Keep cursor at end
        });

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

        forgotPasswordRedirectText.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
