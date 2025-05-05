package com.example.functioninglogin.LoginUI;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.functioninglogin.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.example.functioninglogin.HomePage.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginFragment extends Fragment {
    private static final int RC_SIGN_IN = 9001;

    private EditText loginEmail, loginPassword;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;

    public LoginFragment() { /* Required empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // — Bind views
        loginEmail     = view.findViewById(R.id.login_username);
        loginPassword  = view.findViewById(R.id.login_password);
        passwordToggle = view.findViewById(R.id.password_toggle);
        Button loginButton           = view.findViewById(R.id.login_button);
        TextView signupRedirectText  = view.findViewById(R.id.loginRedirectText);
        TextView forgotRedirectText  = view.findViewById(R.id.forgot_passwordRedirectText);
        SignInButton googleSignInButton = view.findViewById(R.id.googleSignInBtn);

        // — Firebase & Google Sign-In setup
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        )
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // — Password toggle
        passwordToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            int type = isPasswordVisible
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            loginPassword.setInputType(type);
            loginPassword.setSelection(loginPassword.getText().length());
            passwordToggle.setImageResource(
                    isPasswordVisible ? R.drawable.eye_open : R.drawable.blind_eye
            );
        });

        // — Email/password login
        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String pass  = loginPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(getContext(),
                        "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            launchMainActivity();
                        } else {
                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Unknown error";
                            Toast.makeText(getContext(),
                                    "Login Failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // — Google login with forced account chooser
        googleSignInButton.setOnClickListener(v -> {
            // Sign out from Google to force account selection
            mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), signOutTask -> {
                // After sign-out, start the sign-in intent
                Intent intent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            });
        });

        // — Navigation targets
        signupRedirectText.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_fragment_container, new SignupFragment())
                        .addToBackStack(null)
                        .commit()
        );
        forgotRedirectText.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_fragment_container, new ForgotPasswordFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }

    // — Handle Google Sign-In result
    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_SIGN_IN) return;

        Task<GoogleSignInAccount> task =
                GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null) {
                Toast.makeText(getContext(),
                        "Google Sign-In failed: No account selected",
                        Toast.LENGTH_LONG).show();
                return;
            }
            String idToken = account.getIdToken();
            AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);
            auth.signInWithCredential(cred)
                    .addOnCompleteListener(requireActivity(), authTask -> {
                        if (authTask.isSuccessful()) {
                            launchMainActivity();
                        } else {
                            String errorMessage = authTask.getException() != null
                                    ? authTask.getException().getMessage()
                                    : "Unknown error";
                            Toast.makeText(getContext(),
                                    "Google Sign-In failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (ApiException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            Toast.makeText(getContext(),
                    "Google sign in error: " + errorMessage,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void launchMainActivity() {
        Intent i = new Intent(requireContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        requireActivity().finish();
    }
}