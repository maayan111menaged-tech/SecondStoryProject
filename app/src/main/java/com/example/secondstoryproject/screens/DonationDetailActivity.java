package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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
import com.google.android.material.chip.Chip;

public class DonationDetailActivity extends BaseActivity {

    private ImageView ivDonation, ivStatus;
    private TextView tvName, tvDescription, tvStatus;
    private Button btnApprove, btnReject, btnInterested;
    private LinearLayout layout_admin_actions, layout_interested;
    private Donation currentDonation;
    private User currentUser;
    private Chip chipCategory;

    // 🔧 FIX 1: הצהרה על כל ה-Views שחסרו
    private LinearLayout layoutGiverRow, layoutRejectionReason;
    private TextView tvRejectionReason, tvGiverName;
    private ImageView ivGiverAvatar;
    private CardView cardAdminActions, cardInterested;

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
        tvStatus = findViewById(R.id.tvStatus);
        chipCategory = findViewById(R.id.chipCategory);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        layout_admin_actions = findViewById(R.id.layout_admin_actions);

        btnInterested = findViewById(R.id.btnInterested);
        layout_interested = findViewById(R.id.layout_interested);

        // 🔧 FIX 1: אתחול כל ה-Views שחסרו
        layoutGiverRow = findViewById(R.id.layoutGiverRow);
        layoutRejectionReason = findViewById(R.id.layoutRejectionReason);
        tvRejectionReason = findViewById(R.id.tvRejectionReason);
        tvGiverName = findViewById(R.id.tvGiverName);
        ivGiverAvatar = findViewById(R.id.ivGiverAvatar);
        cardAdminActions = findViewById(R.id.cardAdminActions);
        cardInterested = findViewById(R.id.cardInterested);

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
        chipCategory.setText(donation.getCategory() != null ? donation.getCategory().name() : "לא מוגדר");

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

        // 🔧 FIX 2: מציגים גם את ה-CardView החיצוני, לא רק את ה-LinearLayout הפנימי
        if (status == DonationStatus.PENDING_APPROVAL && currentUser != null && currentUser.isAdmin()) {
            cardAdminActions.setVisibility(View.VISIBLE);
            layout_admin_actions.setVisibility(LinearLayout.VISIBLE);
            setupButtonsAdmin();
        } else {
            cardAdminActions.setVisibility(View.GONE);
            layout_admin_actions.setVisibility(LinearLayout.GONE);
        }

        // 🔧 FIX 2: מציגים גם את ה-CardView החיצוני, לא רק את ה-LinearLayout הפנימי
        if (status == DonationStatus.APPROVED_AVAILABLE && currentUser != null && !currentUser.isAdmin()
                && !currentUser.getId().equals(currentDonation.getGiverID())) {
            cardInterested.setVisibility(View.VISIBLE);
            layout_interested.setVisibility(LinearLayout.VISIBLE);
            setupButtonsInterested();
        } else {
            cardInterested.setVisibility(View.GONE);
            layout_interested.setVisibility(LinearLayout.GONE);
        }

        // הראה את layoutGiverRow רק כשהמשתמש אינו התורם עצמו
        if (!currentUser.getId().equals(donation.getGiverID())) {
            layoutGiverRow.setVisibility(View.VISIBLE);
            loadGiverDetails(donation.getGiverID());
        } else {
            layoutGiverRow.setVisibility(View.GONE);
        }

        // הצג סיבת דחייה רק לתורם עצמו כשנדחתה
        if (status == DonationStatus.REJECTED &&
                currentUser.getId().equals(donation.getGiverID())) {
            layoutRejectionReason.setVisibility(View.VISIBLE);
            tvRejectionReason.setText(donation.getRejectionReason());
        } else {
            layoutRejectionReason.setVisibility(View.GONE);
        }
    }

    // 🔧 הוספה: פונקציה נפרדת לטעינת פרטי התורם בשורת הגיבר
    private void loadGiverDetails(String giverId) {
        databaseService.getUserService().get(giverId, new IDatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User giver) {
                runOnUiThread(() -> {
                    if (giver != null) {
                        tvGiverName.setText(giver.getFullName());
                        String avatar = giver.getProfilePhoneUrl();
                        if (avatar != null && !avatar.isEmpty()) {
                            ivGiverAvatar.setImageBitmap(ImageUtil.fromBase64(avatar));
                        }
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                // נשאר עם ערך ברירת מחדל "שם תורם"
            }
        });
    }

    private void setupButtonsAdmin() {
        btnApprove.setOnClickListener(v -> showConfirmDialog(true));
        btnReject.setOnClickListener(v -> showConfirmDialog(false));
    }

    private void setupButtonsInterested() {
        btnInterested.setOnClickListener(v -> interestedFunction());
    }

    private void showConfirmDialog(boolean isApprove) {
        if (isApprove) {
            new AlertDialog.Builder(this)
                    .setTitle("אישור תרומה")
                    .setMessage("לאשר את התרומה?")
                    .setPositiveButton("כן", (dialog, which) ->
                            updateDonationStatus(DonationStatus.APPROVED_AVAILABLE, null))
                    .setNegativeButton("ביטול", null)
                    .show();
        } else {
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

    private void updateDonationStatus(DonationStatus newStatus, String reason) {
        if (currentDonation == null) return;

        currentDonation.updateStatus(newStatus, reason);

        DatabaseService.getInstance().getDonationService().update(
                currentDonation.getId(),
                donation -> currentDonation,
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

    private void interestedFunction() {
        if (currentDonation == null || currentUser == null) return;

        databaseService.getUserService().get(currentDonation.getGiverID(),
                new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User giver) {
                        runOnUiThread(() -> {
                            String displayName = giver != null ? giver.getFullName() : "התורם";
                            showInterestedDialog(displayName);
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> showInterestedDialog("התורם"));
                    }
                });
    }

    private void showInterestedDialog(String giverName) {
        new AlertDialog.Builder(this)
                .setTitle("פתיחת שיחה")
                .setMessage("תיפתח שיחה עם " + giverName + ". להמשיך?")
                // 🔧 FIX 3: מעבירים את giverName לפונקציה
                .setPositiveButton("כן, בואו נדבר!", (dialog, which) -> openOrCreateChat(giverName))
                .setNegativeButton("ביטול", null)
                .show();
    }

    // 🔧 FIX 3: מקבלים את שם התורם כפרמטר במקום להעביר את ה-ID
    private void openOrCreateChat(String giverName) {
        DatabaseService.getInstance().getChatService()
                .getOrCreateDonationChat(
                        currentDonation.getId(),
                        currentDonation.getGiverID(),
                        currentUser.getId(),
                        new IDatabaseService.DatabaseCallback<String>() {
                            @Override
                            public void onCompleted(String chatId) {
                                runOnUiThread(() -> {
                                    Intent intent = new Intent(
                                            DonationDetailActivity.this,
                                            ChatActivity.class);
                                    intent.putExtra("CHAT_ID", chatId);
                                    intent.putExtra("OTHER_USER_NAME", giverName); // ✅ שם אמיתי
                                    startActivity(intent);
                                });
                            }

                            @Override
                            public void onFailed(Exception e) {
                                runOnUiThread(() ->
                                        Toast.makeText(DonationDetailActivity.this,
                                                "שגיאה בפתיחת הצאט", Toast.LENGTH_SHORT).show());
                            }
                        });
    }
}