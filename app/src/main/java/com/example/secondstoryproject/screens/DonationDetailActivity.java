package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.User;
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
                runOnUiThread(() ->
                        Toast.makeText(DonationDetailActivity.this, "שגיאה בטעינת פרטי התרומה", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showDonationDetails(Donation donation) {
        currentDonation = donation;
        currentUser = SharedPreferencesUtil.getUser(this);

        tvName.setText(donation.getName());
        tvDescription.setText(donation.getDescription());
        tvCategory.setText(donation.getCategory() != null ? donation.getCategory().name() : "לא מוגדר");

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

        // הצגת כפתורים למנהל בלבד כאשר הסטטוס הוא מחכה לאישור מנהל
        if (status == DonationStatus.PENDING_APPROVAL && currentUser != null && currentUser.isAdmin()) {
            layoutAdminActions.setVisibility(LinearLayout.VISIBLE);
            setupButtons();
        } else {
            layoutAdminActions.setVisibility(LinearLayout.GONE);
        }
    }

    private void setupButtons() {
        btnApprove.setOnClickListener(v -> showConfirmDialog(true));
        btnReject.setOnClickListener(v -> showConfirmDialog(false));
    }

    private void showConfirmDialog(boolean isApprove) {
        if (isApprove) {
            // אישור
            new AlertDialog.Builder(this)
                    .setTitle("אישור תרומה")
                    .setMessage("לאשר את התרומה?")
                    .setPositiveButton("כן", (dialog, which) ->
                            updateDonationStatus(DonationStatus.APPROVED_AVAILABLE, null))
                    .setNegativeButton("ביטול", null)
                    .show();
        } else {
            // דחייה עם סיבה
            EditText input = new EditText(this);
            input.setHint("כתוב סיבה לדחייה...");

            new AlertDialog.Builder(this)
                    .setTitle("דחיית תרומה")
                    .setView(input)
                    .setPositiveButton("שלח", (dialog, which) -> {
                        String reason = input.getText().toString().trim();
                        if (reason.isEmpty()) {
                            Toast.makeText(this, "חייב לכתוב סיבה", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        updateDonationStatus(DonationStatus.REJECTED, reason);
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        }
    }

    /**
     * פונקציה אחת לעדכון סטטוס – גם אישור וגם דחייה
     * משתמשת ב-donation.updateStatus() כדי לשמור גם היסטוריה
     */
    private void updateDonationStatus(DonationStatus newStatus, String reason) {
        if (currentDonation == null) return;

        currentDonation.updateStatus(newStatus, reason);

        DatabaseService.getInstance().getDonationService().update(
                currentDonation.getId(),
                donation -> currentDonation, // מחזירים את ה-donation עם הסטטוס החדש
                new IDatabaseService.DatabaseCallback<Donation>() {
                    @Override
                    public void onCompleted(Donation updatedDonation) {
                        runOnUiThread(() -> {
                            Toast.makeText(DonationDetailActivity.this,
                                    newStatus == DonationStatus.APPROVED_AVAILABLE
                                            ? "התרומה אושרה ✅"
                                            : "התרומה נדחתה ❌",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(DonationDetailActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }
}