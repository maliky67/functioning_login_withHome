package com.example.functioninglogin.HomePage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.functioninglogin.HomePage.Notifications.NotificationFragment;
import com.example.functioninglogin.HomePage.Notifications.NotificationManager;
import com.example.functioninglogin.NavDrawer.AboutUsActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.example.functioninglogin.GeneratorPage.GeneratorFragment;
import com.example.functioninglogin.LoginUI.AuthActivity;
import com.example.functioninglogin.NavDrawer.LocaleHelper;
import com.example.functioninglogin.NavDrawer.SettingsFragment;
import com.example.functioninglogin.NavDrawer.ShareFragment;
import com.example.functioninglogin.R;
import com.example.functioninglogin.BudgetPage.BudgetFragment;
import com.example.functioninglogin.DiscountPage.DiscountsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private GoogleSignInClient mGoogleSignInClient;

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

        // 🧠 Set user name in nav drawer header
        View headerView = navigationView.getHeaderView(0);
        TextView welcomeText = headerView.findViewById(R.id.navHeaderWelcome); // make sure your TextView has this ID

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            String displayName = user.getDisplayName();
            welcomeText.setText(getString(R.string.welcome) + " " + displayName);
        }

        // Google Sign-In Client setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // Bottom nav setup
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = getSelectedFragment(item);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_toolbar, menu);

        MenuItem notificationItem = menu.findItem(R.id.action_notifications);

        View actionView = notificationItem.getActionView();
        if (actionView == null) {
            actionView = LayoutInflater.from(this).inflate(R.layout.notification_badge_layout, null);
            notificationItem.setActionView(actionView);
        }

        updateNotificationBadge(actionView);

        actionView.setOnClickListener(v -> onOptionsItemSelected(notificationItem));
        return true;
    }

    private void updateNotificationBadge(View actionView) {
        TextView badge = actionView.findViewById(R.id.badge_text);
        int count = NotificationManager.getInstance().getUnreadCount();

        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
        } else {
            badge.setVisibility(View.GONE);
        }
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_fragment_container, new NotificationFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Nullable
    private static Fragment getSelectedFragment(MenuItem item) {
        Fragment selectedFragment = null;

        int id = item.getItemId();
        if (id == R.id.home) {
            selectedFragment = new HomeFragment();
        } else if (id == R.id.Budget) {
            BudgetFragment budgetFragment = new BudgetFragment();
            Bundle args = new Bundle();
            args.putString("listKey", "-ONpmJ4VSnsF6StbrGW1"); // 👈 Replace with your actual listKey
            budgetFragment.setArguments(args);
            selectedFragment = budgetFragment;
        } else if (id == R.id.Discounts) {
            selectedFragment = new DiscountsFragment();
        } else if (id == R.id.Generator) {
            selectedFragment = new GeneratorFragment();
        }
        return selectedFragment;
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
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;

        } else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                finish();
            });
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
