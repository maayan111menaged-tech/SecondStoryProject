package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // קישור ל-TextView במסך
        tvWelcome = findViewById(R.id.tvWelcome);

        // קבלת המשתמש מה-SharedPreferences
        User user = SharedPreferencesUtil.getUser(this);

        if (user != null) {
            String username = user.getUserName();
            Log.d(TAG, "Logged in user: " + username);
            tvWelcome.setText("Welcome, " + username + "!");
        } else {
            Log.d(TAG, "No user is logged in");
            tvWelcome.setText("Welcome, Guest!");
        }


}}
