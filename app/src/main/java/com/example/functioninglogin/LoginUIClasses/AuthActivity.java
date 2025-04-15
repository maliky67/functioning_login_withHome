package com.example.functioninglogin.LoginUIClasses;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.functioninglogin.R;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .commit();
        }
    }
}
