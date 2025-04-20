package com.example.functioninglogin.HomePageUIClasses;

import android.content.Context;
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

import com.example.functioninglogin.GeneratorPageUIClasses.ShoppingListFragment;
import com.example.functioninglogin.LoginUIClasses.AuthActivity;
import com.example.functioninglogin.NavDrawerUIClasses.LocaleHelper;
import com.example.functioninglogin.NavDrawerUIClasses.SettingsFragment;
import com.example.functioninglogin.NavDrawerUIClasses.ShareFragment;
import com.example.functioninglogin.R;
import com.example.functioninglogin.BudgetPageUIClasses.BudgetFragment;
import com.example.functioninglogin.DiscountPageUIClasses.DiscountsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
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
            } else if (id == R.id.Discounts) {
                selectedFragment = new DiscountsFragment();
            } else if (id == R.id.Generator) {
                selectedFragment = new ShoppingListFragment();
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

        Fragment current = getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);

        if (id == R.id.home) {
            if (!(current instanceof HomeFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_fragment_container, new HomeFragment())
                        .commit();
            }
            bottomNavigationView.setSelectedItemId(R.id.home);
            return true;

        } else if (id == R.id.settings) {
            if (!(current instanceof SettingsFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_fragment_container, new SettingsFragment())
                        .commit();
            }
            return true;
        }
        else if (id == R.id.share) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_fragment_container, new ShareFragment())
                    .commit();
            return true;
        }
        else if (id == R.id.about) {
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }
}
