package com.example.secondstoryproject.screens;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminMainActivity extends BaseActivity {

    private Button btnAcceptDonations;
    private Button btnUsersList;
    private Button btnDonationList;
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

        bottomNav.setSelectedItemId(R.id.menu_home);

        btnAcceptDonations = findViewById(R.id.btn_accept_donations);
        btnUsersList = findViewById(R.id.btn_users_list);
        btnDonationList = findViewById(R.id.btn_donation_list);
        tvUsersCount = findViewById(R.id.tv_users_count);
        tvPendingDonations = findViewById(R.id.tv_pending_donations);

        loadStats();

        btnAcceptDonations.setOnClickListener(v ->
                startActivity(new Intent(this, AcceptDonationActivity.class)));
        btnUsersList.setOnClickListener(v ->
                startActivity(new Intent(this, UsersListActivity.class)));
        btnDonationList.setOnClickListener(v ->
                startActivity(new Intent(this, DonationsListActivity.class)));
    }

    private void loadStats() {

        // כמות משתמשים פעילים
        DatabaseService.getInstance().getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        // ✅ סופרים רק משתמשים פעילים
                        int activeCount = 0;
                        Map<String, Boolean> activeMap = new HashMap<>();
                        for (User u : users) {
                            activeMap.put(u.getId(), u.isActive());
                            if (u.isActive()) activeCount++;
                        }
                        tvUsersCount.setText(String.valueOf(activeCount));

                        // ✅ תרומות PENDING רק של תורמים פעילים
                        DatabaseService.getInstance().getDonationService()
                                .getDonationsByStatus(DonationStatus.PENDING_APPROVAL,
                                        new DatabaseService.DatabaseCallback<List<Donation>>() {
                                            @Override
                                            public void onCompleted(List<Donation> donations) {
                                                int count = 0;
                                                for (Donation d : donations) {
                                                    Boolean donorActive = activeMap.get(d.getGiverID());
                                                    if (donorActive == null || donorActive) count++;
                                                }
                                                tvPendingDonations.setText(String.valueOf(count));
                                            }
                                            @Override
                                            public void onFailed(Exception e) {
                                                tvPendingDonations.setText("0");
                                            }
                                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        tvUsersCount.setText("0");
                        tvPendingDonations.setText("0");
                    }
                });
    }
}