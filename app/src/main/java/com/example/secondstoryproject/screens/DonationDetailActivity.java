package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.Rate;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class DonationDetailActivity extends BaseActivity {

    private ImageView ivDonation, ivStatus;
    private TextView tvName, tvDescription, tvStatus;
    private Button btnApprove, btnReject, btnInterested;
    private LinearLayout layout_admin_actions, layout_interested;
    private Donation currentDonation;
    private User currentUser;
    private Chip chipCategory;

    private LinearLayout layoutGiverRow, layoutRejectionReason;
    private LinearLayout layoutReceiverRow;
    private TextView tvRejectionReason, tvGiverName, tvReceiverName;
    private ImageView ivGiverAvatar, ivReceiverAvatar;
    private CardView cardAdminActions, cardInterested;

    private MaterialCardView cardDonationRating;
    private RatingBar ratingBarDonation;
    private TextView tvDonationRatingAvg, tvDonationRatingComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // חץ חזור
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ivDonation      = findViewById(R.id.ivDonation);
        ivStatus        = findViewById(R.id.ivStatus);
        tvName          = findViewById(R.id.tvName);
        tvDescription   = findViewById(R.id.tvDescription);
        tvStatus        = findViewById(R.id.tvStatus);
        chipCategory    = findViewById(R.id.chipCategory);

        btnApprove           = findViewById(R.id.btnApprove);
        btnReject            = findViewById(R.id.btnReject);
        layout_admin_actions = findViewById(R.id.layout_admin_actions);
        btnInterested        = findViewById(R.id.btnInterested);
        layout_interested    = findViewById(R.id.layout_interested);

        layoutGiverRow        = findViewById(R.id.layoutGiverRow);
        layoutReceiverRow     = findViewById(R.id.layoutReceiverRow);
        layoutRejectionReason = findViewById(R.id.layoutRejectionReason);
        tvRejectionReason     = findViewById(R.id.tvRejectionReason);
        tvGiverName           = findViewById(R.id.tvGiverName);
        tvReceiverName        = findViewById(R.id.tvReceiverName);
        ivGiverAvatar         = findViewById(R.id.ivGiverAvatar);
        ivReceiverAvatar      = findViewById(R.id.ivReceiverAvatar);
        cardAdminActions      = findViewById(R.id.cardAdminActions);
        cardInterested        = findViewById(R.id.cardInterested);

        cardDonationRating      = findViewById(R.id.cardDonationRating);
        ratingBarDonation       = findViewById(R.id.ratingBarDonation);
        tvDonationRatingAvg     = findViewById(R.id.tvDonationRatingAvg);
        tvDonationRatingComment = findViewById(R.id.tvDonationRatingComment);

        // צביעת כוכבי RatingBar בקוד (app:tint לא עובד על RatingBar)
        ratingBarDonation.getProgressDrawable()
                .setColorFilter(getColor(R.color.medium_purple), PorterDuff.Mode.SRC_ATOP);

        String donationId = getIntent().getStringExtra("DONATION_ID");
        if (donationId != null) {
            loadDonationDetails(donationId);
        }
    }

    private void loadDonationDetails(String donationId) {
        databaseService.getDonationService().get(donationId,
                new IDatabaseService.DatabaseCallback<Donation>() {
                    @Override
                    public void onCompleted(Donation donation) {
                        if (donation == null) return;
                        runOnUiThread(() -> showDonationDetails(donation));
                    }
                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(DonationDetailActivity.this,
                                        "שגיאה בטעינת פרטי התרומה", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void showDonationDetails(Donation donation) {
        currentDonation = donation;
        currentUser     = SharedPreferencesUtil.getUser(this);

        tvName.setText(donation.getName());
        tvDescription.setText(donation.getDescription());
        chipCategory.setText(donation.getCategory() != null
                ? donation.getCategory().name() : "לא מוגדר");

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

        boolean isClosed = status == DonationStatus.MATCHED
                || status == DonationStatus.CANCELLED
                || status == DonationStatus.DONOR_DELETED
                || status == DonationStatus.REJECTED;

        boolean isGiver = currentUser.getId().equals(donation.getGiverID());
        boolean isAdmin = currentUser.isAdmin();

        // ── כפתורי אדמין ──
        if (status == DonationStatus.PENDING_APPROVAL && isAdmin) {
            cardAdminActions.setVisibility(View.VISIBLE);
            layout_admin_actions.setVisibility(View.VISIBLE);
            setupButtonsAdmin();
        } else {
            cardAdminActions.setVisibility(View.GONE);
            layout_admin_actions.setVisibility(View.GONE);
        }

        // ── כפתור מעוניין — משתמש רגיל, תרומה זמינה, לא התורם ──
        if (status == DonationStatus.APPROVED_AVAILABLE && !isAdmin && !isGiver) {
            cardInterested.setVisibility(View.VISIBLE);
            layout_interested.setVisibility(View.VISIBLE);
            setupInterestedButton();
        } else {
            cardInterested.setVisibility(View.GONE);
            layout_interested.setVisibility(View.GONE);
        }

        // ── שורת תורם — לכולם חוץ מהתורם עצמו ──
        if (!isGiver) {
            layoutGiverRow.setVisibility(View.VISIBLE);
            loadGiverDetails(donation.getGiverID());
        } else {
            layoutGiverRow.setVisibility(View.GONE);
        }

        // ── שורת מקבל — בתרומה סגורה, לתורם ולאדמין, רק אם יש receiverId ──
        if (isClosed && (isGiver || isAdmin)
                && donation.getReceiverID() != null && !donation.getReceiverID().isEmpty()) {
            layoutReceiverRow.setVisibility(View.VISIBLE);
            loadReceiverDetails(donation.getReceiverID());
        } else {
            layoutReceiverRow.setVisibility(View.GONE);
        }

        // ── סיבת דחייה — לתורם עצמו ולאדמין ──
        if (status == DonationStatus.REJECTED && (isGiver || isAdmin)) {
            layoutRejectionReason.setVisibility(View.VISIBLE);
            String reason = donation.getRejectionReason();
            tvRejectionReason.setText(reason != null ? reason : "לא צוינה סיבה");
        } else {
            layoutRejectionReason.setVisibility(View.GONE);
        }

        // ── דירוג ──
        loadDonationRating(donation, isClosed, isGiver, isAdmin);
    }

    private void loadGiverDetails(String giverId) {
        databaseService.getUserService().get(giverId,
                new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User giver) {
                        runOnUiThread(() -> {
                            if (giver != null) {
                                tvGiverName.setText(giver.getFullName());
                                String avatar = giver.getProfilePhoneUrl();
                                if (avatar != null && !avatar.isEmpty()) {
                                    ivGiverAvatar.setImageBitmap(ImageUtil.fromBase64(avatar));
                                }
                                layoutGiverRow.setOnClickListener(v -> {
                                    Intent intent = new Intent(DonationDetailActivity.this,
                                            UserProfileActivity.class);
                                    intent.putExtra("USER_ID", giver.getId());
                                    startActivity(intent);
                                });
                            }
                        });
                    }
                    @Override public void onFailed(Exception e) { }
                });
    }

    private void loadReceiverDetails(String receiverId) {
        databaseService.getUserService().get(receiverId,
                new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User receiver) {
                        runOnUiThread(() -> {
                            if (receiver != null) {
                                tvReceiverName.setText(receiver.getFullName());
                                String avatar = receiver.getProfilePhoneUrl();
                                if (avatar != null && !avatar.isEmpty()) {
                                    ivReceiverAvatar.setImageBitmap(ImageUtil.fromBase64(avatar));
                                }
                                layoutReceiverRow.setOnClickListener(v -> {
                                    Intent intent = new Intent(DonationDetailActivity.this,
                                            UserProfileActivity.class);
                                    intent.putExtra("USER_ID", receiver.getId());
                                    startActivity(intent);
                                });
                            }
                        });
                    }
                    @Override public void onFailed(Exception e) { }
                });
    }

    private void setupButtonsAdmin() {
        btnApprove.setOnClickListener(v -> showConfirmDialog(true));
        btnReject.setOnClickListener(v -> showConfirmDialog(false));
    }

    /**
     * בודק אם יש צ'אט פתוח לתרומה הזו למשתמש הנוכחי.
     * chatId = "donation_{donationId}_{receiverId}"
     * אם יש — פותח ישירות. אם אין — מציג דיאלוג.
     */
    private void setupInterestedButton() {
        String expectedChatId = "donation_" + currentDonation.getId() + "_" + currentUser.getId();

        DatabaseService.getInstance().getChatService()
                .getUserChats(currentUser.getId(),
                        new IDatabaseService.DatabaseCallback<java.util.List<com.example.secondstoryproject.models.Chat>>() {
                            @Override
                            public void onCompleted(java.util.List<com.example.secondstoryproject.models.Chat> chats) {
                                boolean chatExists = chats.stream()
                                        .anyMatch(c -> expectedChatId.equals(c.getId()));

                                runOnUiThread(() -> {
                                    if (chatExists) {
                                        // צ'אט קיים — כפתור פותח ישירות
                                        btnInterested.setText("המשך שיחה עם התורם");
                                        btnInterested.setBackgroundTintList(
                                                ColorStateList.valueOf(getColor(R.color.lavender)));
                                        btnInterested.setTextColor(getColor(R.color.dark_purple));
                                        btnInterested.setOnClickListener(v -> {
                                            Intent i = new Intent(DonationDetailActivity.this,
                                                    ChatActivity.class);
                                            i.putExtra("CHAT_ID", expectedChatId);
                                            i.putExtra("OTHER_USER_ID", currentDonation.getGiverID());
                                            startActivity(i);
                                        });
                                    } else {
                                        // אין צ'אט — כפתור פותח דיאלוג
                                        btnInterested.setText("מעוניין בתרומה זאת");
                                        btnInterested.setBackgroundTintList(
                                                ColorStateList.valueOf(getColor(R.color.dark_purple)));
                                        btnInterested.setTextColor(getColor(R.color.light_lavender));
                                        btnInterested.setOnClickListener(v -> interestedFunction());
                                    }
                                });
                            }
                            @Override
                            public void onFailed(Exception e) {
                                // ברירת מחדל — כפתור פתיחת שיחה
                                runOnUiThread(() ->
                                        btnInterested.setOnClickListener(v -> interestedFunction()));
                            }
                        });
    }

    private void showConfirmDialog(boolean isApprove) {
        if (isApprove) {
            new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle("אישור תרומה")
                    .setMessage("לאשר את התרומה?")
                    .setPositiveButton("כן", (dialog, which) ->
                            updateDonationStatus(DonationStatus.APPROVED_AVAILABLE, null))
                    .setNegativeButton("ביטול", null)
                    .show();
        } else {
            int dp16 = (int) (16 * getResources().getDisplayMetrics().density);
            int dp8  = (int) (8  * getResources().getDisplayMetrics().density);

            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(dp16, dp16, dp16, 0);

            TextView label = new TextView(this);
            label.setText("סיבת הדחייה תוצג לתורם");
            label.setTextSize(13f);
            label.setTextColor(getColor(R.color.text_muted));
            label.setPadding(0, 0, 0, dp8);

            EditText input = new EditText(this);
            input.setHint("כתוב סיבה...");
            input.setMinLines(2);
            input.setBackgroundTintList(
                    ColorStateList.valueOf(getColor(R.color.medium_purple)));

            container.addView(label);
            container.addView(input);

            new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle("דחיית תרומה")
                    .setView(container)
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
                                Toast.makeText(DonationDetailActivity.this,
                                        "שגיאה בעדכון", Toast.LENGTH_SHORT).show());
                    }
                });
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
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("פתיחת שיחה")
                .setMessage("תיפתח שיחה עם " + giverName + ". להמשיך?")
                .setPositiveButton("כן, בואו נדבר!", (dialog, which) -> openOrCreateChat(giverName))
                .setNegativeButton("ביטול", null)
                .show();
    }

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
                                            DonationDetailActivity.this, ChatActivity.class);
                                    intent.putExtra("CHAT_ID", chatId);
                                    intent.putExtra("OTHER_USER_NAME", giverName);
                                    intent.putExtra("OTHER_USER_ID", currentDonation.getGiverID());
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

    private void loadDonationRating(Donation donation, boolean isClosed,
                                    boolean isGiver, boolean isAdmin) {
        // מציג רק לתורם או לאדמין, ורק בתרומה סגורה
        if (!isClosed || (!isGiver && !isAdmin)) {
            cardDonationRating.setVisibility(View.GONE);
            return;
        }

        DatabaseService.getInstance().getRateService()
                .getRatesForUser(donation.getGiverID(),
                        new IDatabaseService.DatabaseCallback<List<Rate>>() {
                            @Override
                            public void onCompleted(List<Rate> rates) {
                                Rate match = null;
                                if (rates != null) {
                                    for (Rate r : rates) {
                                        if (donation.getId().equals(r.getDonationId())) {
                                            match = r;
                                            break;
                                        }
                                    }
                                }
                                final Rate finalMatch = match;
                                runOnUiThread(() -> {
                                    if (finalMatch == null) {
                                        cardDonationRating.setVisibility(View.GONE);
                                        return;
                                    }
                                    cardDonationRating.setVisibility(View.VISIBLE);
                                    ratingBarDonation.setRating(finalMatch.getStarAmount());
                                    // מציג רק את מספר הכוכבים המלא, בלי חלוקת 5
                                    tvDonationRatingAvg.setText(finalMatch.getStarAmount() + " ★");

                                    String comment = finalMatch.getComment();
                                    if (comment != null && !comment.isEmpty()) {
                                        tvDonationRatingComment.setVisibility(View.VISIBLE);
                                        tvDonationRatingComment.setText("\"" + comment + "\"");
                                    } else {
                                        tvDonationRatingComment.setVisibility(View.GONE);
                                    }
                                });
                            }
                            @Override
                            public void onFailed(Exception e) {
                                runOnUiThread(() ->
                                        cardDonationRating.setVisibility(View.GONE));
                            }
                        });
    }
}