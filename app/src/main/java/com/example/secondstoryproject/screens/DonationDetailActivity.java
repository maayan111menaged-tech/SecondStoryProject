package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;

public class DonationDetailActivity extends BaseActivity {

    private ImageView ivDonation, ivStatus;
    private TextView tvName, tvDescription, tvCategory, tvStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail);

        ivDonation = findViewById(R.id.ivDonation);
        ivStatus = findViewById(R.id.ivStatus);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvStatus = findViewById(R.id.tvStatus);

        String donationId = getIntent().getStringExtra("donationId");
        if (donationId != null) {
            loadDonationDetails(donationId);
        }
    }

    private void loadDonationDetails(String donationId) {

        databaseService.getDonationService().get(donationId, new IDatabaseService.DatabaseCallback<Donation>() {

            @Override
            public void onCompleted(Donation donation) {

                if (donation == null) return;

                runOnUiThread(() -> showDonationDetails(donation));
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }
    private void showDonationDetails(Donation donation) {

        tvName.setText(donation.getName());
        tvDescription.setText(donation.getDescription());

        if (donation.getCategory() != null) {
            tvCategory.setText(donation.getCategory().name());
        } else {
            tvCategory.setText("לא מוגדר");
        }

        DonationStatus status = donation.getStatus();

        if (status != null) {
            tvStatus.setText(status.getHebrewName());
            ivStatus.setImageResource(status.getIconResId());
        }

        String photo = donation.getPhotoUrl();

        if (photo != null && !photo.isEmpty()) {
            ivDonation.setImageBitmap(ImageUtil.fromBase64(photo));
        } else {
            ivDonation.setImageResource(R.drawable.ic_profile);
        }
    }
}