package com.example.functioninglogin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.functioninglogin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class TestingForgotPassword extends AppCompatActivity {

    private EditText forgotEmail;
    private Button resetButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_forgot_password);

        forgotEmail = findViewById(R.id.forgot_email);
        resetButton = findViewById(R.id.btn_reset_password);
        auth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = forgotEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(TestingForgotPassword.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(TestingForgotPassword.this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                                    finish(); // optional: close this activity
                                } else {
                                    Toast.makeText(TestingForgotPassword.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}
