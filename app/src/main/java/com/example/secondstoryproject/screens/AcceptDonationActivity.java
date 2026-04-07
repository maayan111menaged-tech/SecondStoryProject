package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import com.example.secondstoryproject.services.DatabaseService;

import java.util.Date;
import java.util.List;

public class AcceptDonationActivity extends BaseActivity {

    private static final String TAG = "AcceptDonationActivity";

    private RecyclerView rvDonations;
    private DonationAdapter adapter;

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

        DatabaseService.getInstance()
                .getDonationService()
                .getDonationsByStatus(DonationStatus.PENDING_APPROVAL,
                        new DatabaseService.DatabaseCallback<List<Donation>>() {

                            @Override
                            public void onCompleted(List<Donation> donations) {
                                Log.d(TAG, "Loaded donations: " + donations.size());

                                // מיון לפי תאריך הפרסום - הישן ביותר ראשון
                                donations.sort((a, b) -> {
                                    Date dateA = getFirstStatusDate(a);
                                    Date dateB = getFirstStatusDate(b);
                                    if (dateA == null) return 1;
                                    if (dateB == null) return -1;
                                    return dateA.compareTo(dateB);
                                });

                                adapter.setDonations(donations);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Log.e(TAG, "Failed to load donations", e);
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