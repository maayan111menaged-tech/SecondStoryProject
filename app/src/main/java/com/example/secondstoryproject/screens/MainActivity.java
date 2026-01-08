package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.UserLevel;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ProgressBar rateProgressBar;
    ImageView currentRateIcon,nextRateIcon;
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



}
