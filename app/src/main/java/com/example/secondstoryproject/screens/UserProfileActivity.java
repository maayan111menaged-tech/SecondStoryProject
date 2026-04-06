package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.ProfileDonationAdapter;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.UserLevel;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends BaseActivity {

    private TextView tvUserName, tvFullName, tvLevel,  tvPhone, tvEmail, tvBirthday;
    private ImageView ivProfile, ivLevel;
    private Button btnEdit;

    private User currentUser;

    private RecyclerView rvUserDonations;
    private ProfileDonationAdapter donationAdapter;
    private List<Donation> userDonations;

    private ImageButton btnLeft, btnRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bottomNav.setSelectedItemId(R.id.menu_profile);


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

        rvUserDonations = findViewById(R.id.rvUserDonations);
        userDonations = new ArrayList<>();
        rvUserDonations.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        donationAdapter = new ProfileDonationAdapter(this, userDonations);
        rvUserDonations.setAdapter(donationAdapter);

        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        btnLeft.setOnClickListener(v -> rvUserDonations.smoothScrollBy(300, 0));
        btnRight.setOnClickListener(v -> rvUserDonations.smoothScrollBy(-300, 0));

        // קבלת המשתמש מהSharedPreferences
        currentUser = SharedPreferencesUtil.getUser(this);

        // הצגת הפרטים בשדות
        if (currentUser != null) {
            showUserProfile();
            loadUserDonations();
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
    private void loadUserDonations() {

        databaseService.getDonationService().getByGiverId(
                currentUser.getId(),
                new IDatabaseService.DatabaseCallback<List<Donation>>() {

                    @Override
                    public void onCompleted(List<Donation> donations) {

                        userDonations.clear();
                        userDonations.addAll(donations);
                        donationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                }
        );

    }
    private void scrollRecycler(int direction) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvUserDonations.getLayoutManager();
        if (layoutManager != null) {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int target = firstVisible + direction;
            if (target < 0) target = 0;
            if (target >= donationAdapter.getItemCount()) target = donationAdapter.getItemCount() - 1;
            rvUserDonations.smoothScrollToPosition(target);
        }
    }
}