package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.DonationAdapter;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcceptDonationActivity extends BaseActivity {

    private static final String TAG = "AcceptDonationActivity";

    private RecyclerView rvDonations;
    private DonationAdapter adapter;
    private LinearLayout layoutEmpty;
    private TextView tvDonationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_accept_donation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvDonations = findViewById(R.id.rv_donations);
        rvDonations.setLayoutManager(new LinearLayoutManager(this));

        layoutEmpty = findViewById(R.id.layout_empty);
        tvDonationCount = findViewById(R.id.tv_donation_to_accept_count);

        // מעבר לפרטי התרומה
        adapter = new DonationAdapter(donation -> {
            Intent intent = new Intent(AcceptDonationActivity.this, DonationDetailActivity.class);
            intent.putExtra("DONATION_ID", donation.getId());
            startActivity(intent);
        });

        rvDonations.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonations();
    }

    private void loadDonations() {
        // שלב 1: טוענים את כל המשתמשים לבניית מפה uid → isActive
        DatabaseService.getInstance().getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        Map<String, Boolean> activeMap = new HashMap<>();
                        for (User u : users) {
                            activeMap.put(u.getId(), u.isActive());
                        }

                        // שלב 2: טוענים תרומות לפי סטטוס
                        DatabaseService.getInstance()
                                .getDonationService()
                                .getDonationsByStatus(DonationStatus.PENDING_APPROVAL,
                                        new DatabaseService.DatabaseCallback<List<Donation>>() {
                                            @Override
                                            public void onCompleted(List<Donation> donations) {
                                                // ✅ פילטור תרומות של תורמים לא פעילים
                                                List<Donation> filtered = new ArrayList<>();
                                                for (Donation d : donations) {
                                                    Boolean donorActive = activeMap.get(d.getGiverID());
                                                    boolean isDonorActive = donorActive == null || donorActive;
                                                    if (isDonorActive) {
                                                        filtered.add(d);
                                                    }
                                                }

                                                // מיון לפי תאריך – הישן ביותר ראשון
                                                filtered.sort((a, b) -> {
                                                    Date dateA = getFirstStatusDate(a);
                                                    Date dateB = getFirstStatusDate(b);
                                                    if (dateA == null) return 1;
                                                    if (dateB == null) return -1;
                                                    return dateA.compareTo(dateB);
                                                });

                                                tvDonationCount.setText("Total: " + filtered.size());
                                                adapter.setDonations(filtered);

                                                if (filtered.isEmpty()) {
                                                    rvDonations.setVisibility(View.GONE);
                                                    layoutEmpty.setVisibility(View.VISIBLE);
                                                } else {
                                                    rvDonations.setVisibility(View.VISIBLE);
                                                    layoutEmpty.setVisibility(View.GONE);
                                                }
                                            }

                                            @Override
                                            public void onFailed(Exception e) {
                                                Log.e(TAG, "Failed to load donations", e);
                                            }
                                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to load users", e);
                    }
                });
    }

    private Date getFirstStatusDate(Donation donation) {
        if (donation.getStatusHistory() == null || donation.getStatusHistory().isEmpty()) {
            return null;
        }
        return donation.getStatusHistory().get(0).getTimestamp();
    }
}