package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.UserLevel;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

public class UserProfileActivity extends BaseActivity {

    private TextView tvUserName, tvFullName, tvLevel,  tvPhone, tvEmail, tvBirthday;
    private ImageView ivProfile, ivLevel;
    private Button btnEdit;

    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvUserName = findViewById(R.id.tvUserName);
        tvFullName = findViewById(R.id.tvFullName);
        tvLevel = findViewById(R.id.tvLevel);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvBirthday = findViewById(R.id.tvBirthday);

        ivProfile = findViewById(R.id.imgProfile);
        ivLevel = findViewById(R.id.imgMedal);

        btnEdit = findViewById(R.id.btnEditProfile);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, updateDetailsActivity.class);
                startActivity(intent);
            }
        });

        // קבלת המשתמש מהSharedPreferences
        currentUser = SharedPreferencesUtil.getUser(this);

        // הצגת הפרטים בשדות
        if (currentUser != null) {
            showUserProfile();
        }


    }
    private void showUserProfile() {
        tvUserName.setText(currentUser.getUserName());
        tvFullName.setText(currentUser.getFullName());
        tvPhone.setText(currentUser.getPhoneNumber());
        tvEmail.setText(currentUser.getEmail());
        tvBirthday.setText(currentUser.getDateOfBirth());

        String profileImageBase64 = currentUser.getProfilePhoneUrl();

        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            ivProfile.setImageBitmap(ImageUtil.fromBase64(profileImageBase64));
        }

        UserLevel currentLevel = UserLevel.fromDonationCount(currentUser.getDonationCounter());
        ivLevel.setImageResource(currentLevel.getIconRes());
        tvLevel.setText(currentLevel.getLabel());


    }
}