package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

public class DonationDetailActivity extends BaseActivity {

    private ImageView ivDonation, ivStatus;
    private TextView tvName, tvDescription, tvCategory, tvStatus;

    private Button btnApprove, btnReject;
    private LinearLayout layoutAdminActions;
    private Donation currentDonation;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivDonation = findViewById(R.id.ivDonation);
        ivStatus = findViewById(R.id.ivStatus);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvStatus = findViewById(R.id.tvStatus);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        layoutAdminActions = findViewById(R.id.layout_admin_actions);

        String donationId = getIntent().getStringExtra("DONATION_ID");
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

        currentDonation = donation;
        currentUser = SharedPreferencesUtil.getUser(this);

        if (status == DonationStatus.PENDING_APPROVAL
                && currentUser != null
                && currentUser.isAdmin()) {
            layoutAdminActions.setVisibility(View.VISIBLE);
            setupButtons();
        } else {
            layoutAdminActions.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        btnApprove.setOnClickListener(v -> showConfirmDialog(true));
        btnReject.setOnClickListener(v -> showConfirmDialog(false));
    }

    private void showConfirmDialog(boolean isApprove) {

        String message = isApprove ? "לאשר את התרומה?" : "לדחות את התרומה?";

        new AlertDialog.Builder(this)
                .setTitle("אישור פעולה")
                .setMessage(message)
                .setPositiveButton("כן", (dialog, which) -> {
                    if (isApprove) {
                        updateStatus(DonationStatus.APPROVED_AVAILABLE);
                    } else {
                        updateStatus(DonationStatus.REJECTED);
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void updateStatus(DonationStatus newStatus) {

        DatabaseService.getInstance().getDonationService().update(
                currentDonation.getId(),
                donation -> {
                    donation.setStatus(newStatus);
                    return donation;
                },
                new IDatabaseService.DatabaseCallback<Donation>() {

                    @Override
                    public void onCompleted(Donation updatedDonation) {

                        runOnUiThread(() -> {
                            Toast.makeText(DonationDetailActivity.this,
                                    newStatus == DonationStatus.APPROVED_AVAILABLE
                                            ? "התרומה אושרה ✅"
                                            : "התרומה נדחתה ❌",
                                    Toast.LENGTH_SHORT).show();

                            finish(); // חזרה לרשימה
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {

                        runOnUiThread(() ->
                                Toast.makeText(DonationDetailActivity.this,
                                        "שגיאה בעדכון",
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }
}