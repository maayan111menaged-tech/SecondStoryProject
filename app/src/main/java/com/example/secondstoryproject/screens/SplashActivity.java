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

public class SplashActivity extends BaseActivity {
    @Override
    protected boolean hasSideMenu() {
        return false; // לא צריך Drawer
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPreferencesUtil.isUserLoggedIn(SplashActivity.this)) {
                    User currentUser = SharedPreferencesUtil.getUser(SplashActivity.this);
                    Class<?> targetActivity = currentUser.isAdmin()
                            ? AdminMainActivity.class
                            : MainActivity.class;
                    startActivity(new Intent(SplashActivity.this, targetActivity));
                } else {
                    startActivity(new Intent(SplashActivity.this, LandingActivity.class));
                }
                finish();
            }
        }, 3000);
    }
}