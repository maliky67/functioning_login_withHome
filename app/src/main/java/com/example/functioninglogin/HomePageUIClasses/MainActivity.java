package com.example.functioninglogin.HomePageUIClasses;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.LibraryFragment;
import com.example.functioninglogin.LoginUIClasses.AuthActivity;
import com.example.functioninglogin.LoginUIClasses.LoginFragment;
import com.example.functioninglogin.R;
import com.example.functioninglogin.BudgetFragment;
import com.example.functioninglogin.SubscriptionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Bottom nav setup
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.Budget) {
                // ✅ Create BudgetFragment and attach listKey
                BudgetFragment budgetFragment = new BudgetFragment();
                Bundle args = new Bundle();
                args.putString("listKey", "-ONpmJ4VSnsF6StbrGW1"); // 👈 Replace with your actual listKey
                budgetFragment.setArguments(args);
                selectedFragment = budgetFragment;
            } else if (id == R.id.subscriptions) {
                selectedFragment = new SubscriptionsFragment();
            } else if (id == R.id.library) {
                selectedFragment = new LibraryFragment();
            }

            if (selectedFragment != null) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);

                if (!(currentFragment != null && currentFragment.getClass().equals(selectedFragment.getClass()))) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.home_fragment_container, selectedFragment)
                            .commit();
                }

                return true;
            }

            return false;
        });

        // Default tab
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        int id = item.getItemId();
        if (id == R.id.home) {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);
            if (!(current instanceof HomeFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_fragment_container, new HomeFragment())
                        .commit();
            }

            bottomNavigationView.setSelectedItemId(R.id.home);
            return true;

        } else if (id == R.id.settings || id == R.id.about || id == R.id.share) {
            Toast.makeText(this, "Coming Soon! 🚧", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
