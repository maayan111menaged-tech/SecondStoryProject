package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DatabaseService databaseService;
    protected DrawerLayout drawerLayout;

    protected boolean hasSideMenu() {
        return true; // ברירת מחדל – יש Drawer
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseService = DatabaseService.getInstance();

        // טוען את ה-Base XML
        setContentView(R.layout.activity_base);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        // Drawer
        drawerLayout = findViewById(R.id.nav_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (hasSideMenu()) {

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setTitle("");
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_24);
            }

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    toolbar,
                    R.string.open_drawer,
                    R.string.close_drawer
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        } else{
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            navigationView.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("");
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeAsUpIndicator(null);
            }
        }
    }

    // 👇 מזריק את ה-layout של המסך לתוך Base
    protected void setContentLayout(int layoutResId) {
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResId, contentFrame, true);
    }

    protected void navigateTo(Class<?> targetActivity) {

        if (!this.getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {

            navigateTo(MainActivity.class);

        } else if (id == R.id.nav_user_profile) {

            ///navigateTo(UserProfileActivity.class);

        } else if (id == R.id.nav_add_donation) {

            ///navigateTo(AddDonationActivity.class);

        } else if (id == R.id.nav_search_donation) {

            ///navigateTo(SearchDonationActivity.class);

        } else if (id == R.id.nav_leaders_board) {

            navigateTo(LeaderBoardActivity.class);

        } else if (id == R.id.nav_settings) {

            ///navigateTo(SettingsActivity.class);

        } else if (id == R.id.nav_signOut) {

            drawerLayout.closeDrawer(GravityCompat.START);
            showLogoutDialog();
        }
        return true;
    }
    private void showLogoutDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    SharedPreferencesUtil.signOutUser(this);

                    Intent intent = new Intent(this, LandingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
