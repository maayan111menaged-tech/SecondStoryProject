package com.example.secondstoryproject.screens;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.UserLevel;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "MainActivity";
    ProgressBar rateProgressBar;
    ImageView currentRateIcon,nextRateIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);

        rateProgressBar = findViewById(R.id.rateProgressBar);
        currentRateIcon = findViewById(R.id.currentRateIcon);
        nextRateIcon = findViewById(R.id.nextRateIcon);

        User currentUser = SharedPreferencesUtil.getUser(this);

        updateUserLevelUI(currentUser);
    }


    private void updateUserLevelUI(User user) {

        int donations = user.getDonationCounter();
        UserLevel currentLevel = UserLevel.fromDonationCount(donations);

        int min = currentLevel.getMinDonations();
        int max = currentLevel.getMaxDonations()+1;

        int progress;
        if (max == Integer.MAX_VALUE) {
            progress = 100;
        } else {
            progress = (donations - min) * 100 / (max - min);
        }

        rateProgressBar.setProgress(progress);

        // אייקון דרגה נוכחית
        currentRateIcon.setImageResource(currentLevel.getIconRes());

        // אייקון דרגה הבאה
        UserLevel[] levels = UserLevel.values();
        int nextIndex = currentLevel.ordinal() + 1;

        if (nextIndex < levels.length) {
            nextRateIcon.setVisibility(View.VISIBLE);
            nextRateIcon.setImageResource(levels[nextIndex].getIconRes());
        } else {
            nextRateIcon.setVisibility(View.INVISIBLE); // אגדה
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

