package com.example.secondstoryproject.screens;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;

import com.example.secondstoryproject.R;

public class AdminMainActivity extends BaseActivity {

    private Button btnAcceptDonations;
    private Button btnUsersList;

    private TextView tvUsersCount;
    private TextView tvPendingDonations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 חיבורים ל-XML
        btnAcceptDonations = findViewById(R.id.btn_accept_donations);
        btnUsersList = findViewById(R.id.btn_users_list);

        tvUsersCount = findViewById(R.id.tv_users_count);
        tvPendingDonations = findViewById(R.id.tv_pending_donations);

        // 🔹 טעינת נתונים
        loadStats();

        // 🔹 כפתורים
        ///btnAcceptDonations.setOnClickListener(v ->
           ///     startActivity(new Intent(this, AcceptDonationActivity.class)));

        btnUsersList.setOnClickListener(v ->
                startActivity(new Intent(this, UsersListActivity.class)));
    }

    // 🔥 פונקציה שמביאה נתונים
    private void loadStats() {

        // ✔ כמות משתמשים
        DatabaseService.getInstance().getUserService().getUsersCount(new DatabaseService.DatabaseCallback<Integer>() {
            @Override
            public void onCompleted(Integer count) {
                tvUsersCount.setText(String.valueOf(count));
            }

            @Override
            public void onFailed(Exception e) {
                tvUsersCount.setText("0");
            }
        });

        // ✔ כמות תרומות ממתינות לאישור
        DatabaseService.getInstance().getDonationService()
                .getDonationsCountByStatus(DonationStatus.PENDING_APPROVAL,
                        new DatabaseService.DatabaseCallback<Integer>() {
                            @Override
                            public void onCompleted(Integer count) {
                                tvPendingDonations.setText(String.valueOf(count));
                            }

                            @Override
                            public void onFailed(Exception e) {
                                tvPendingDonations.setText("0");
                            }
                        });
    }
}