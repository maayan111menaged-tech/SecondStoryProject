package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

// Splash screen — shown on app launch for 3 seconds, then navigates to the correct screen.
// No drawer or bottom nav needed here.

public class SplashActivity extends BaseActivity {
    @Override
    protected boolean hasSideMenu() {
        return false;
    }
    @Override
    protected boolean hasBottomMenu(){ return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // waits 3 seconds, then navigates to the correct screen based on login state
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPreferencesUtil.isUserLoggedIn(SplashActivity.this)) {
                    User currentUser = SharedPreferencesUtil.getUser(SplashActivity.this);
                    // sends admin to AdminMainActivity, regular user to MainActivity
                    Class<?> targetActivity = currentUser.isAdmin()
                            ? AdminMainActivity.class
                            : MainActivity.class;
                    startActivity(new Intent(SplashActivity.this, targetActivity));
                } else {
                    // no logged-in user — go to landing screen
                    startActivity(new Intent(SplashActivity.this, LandingActivity.class));
                }
                // closes SplashActivity so the user can't navigate back to it
                finish();
            }
        }, 3000);
    }
}