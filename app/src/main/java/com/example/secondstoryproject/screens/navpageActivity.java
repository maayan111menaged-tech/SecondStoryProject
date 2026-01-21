package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import com.example.secondstoryproject.R;
import com.google.android.material.navigation.NavigationView;

public class navpageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-Edge (לא חובה אם לא צריך)
        setContentView(R.layout.activity_navpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // -----------------------------
        // הגדרת ה-Toolbar
        // -----------------------------
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        // מחביא את הכותרת
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // -----------------------------
        // הגדרת DrawerLayout
        // -----------------------------
        drawerLayout = findViewById(R.id.nav_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // יצירת האייקון של 3 פסים (Hamburger Menu)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.black));

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // -----------------------------
        // טעינת Fragment ראשוני
        // -----------------------------
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_containter, new DialogFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // סגירת ה-Drawer אחרי בחירה
        drawerLayout.closeDrawer(GravityCompat.START);

        // TODO: חיבור לעמודים שונים בהתאם ל-item.getItemId()
        return true;
    }

    @Override
    public void onBackPressed() {
        // אם ה-Drawer פתוח, סוגר אותו במקום לצאת
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
