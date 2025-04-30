package com.example.functioninglogin.HomePageUIClasses;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.functioninglogin.R;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);  // Esta vista la creamos en el próximo paso
        setTitle("About Us");
    }
}
