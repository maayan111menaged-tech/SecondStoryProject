package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.ProfileDonationAdapter;
import com.example.secondstoryproject.models.Chat;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.Rate;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.UserLevel;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * עמוד פרופיל – 5 מצבים:
 *  SELF        – משתמש רגיל רואה את עצמו
 *  SELF_ADMIN  – אדמין רואה את עצמו
 *  OTHER_USER  – משתמש רגיל רואה אחר (פרטים מצומצמים, תרומות זמינות בלבד)
 *  OTHER_ADMIN – אדמין רואה משתמש אחר (כל הפרטים + כל התרומות)
 *  ADMIN_ADMIN – אדמין רואה אדמין אחר
 */
public class UserProfileActivity extends BaseActivity {

    @Override
    protected int getSelectedBottomNavItem() {
        String targetUserId = getIntent().getStringExtra("USER_ID");
        User currentUser = SharedPreferencesUtil.getUser(this);
        boolean viewingSelf = targetUserId == null
                || (currentUser != null && targetUserId.equals(currentUser.getId()));
        return viewingSelf ? R.id.menu_profile : -1;
    }

    private enum ProfileMode { SELF, SELF_ADMIN, OTHER_USER, OTHER_ADMIN, ADMIN_ADMIN }
    private ProfileMode mode;

    // Views
    private TextView   tvPageTitle, tvUserName, tvFullName, tvLevel, tvContactTitle;
    private TextView   tvPhone, tvEmail, tvBirthday, tvDonationCount, tvAdminState;
    private ImageView  ivProfile, ivLevel;
    private Button     btnEdit, btnChat, btnViewAllDonations;
    private LinearLayout layoutChatRow, layoutDonationsSection, layoutPublicInfo;
    private RecyclerView rvUserDonations;
    private android.widget.ImageButton btnLeft, btnRight;
    private com.google.android.material.card.MaterialCardView layoutContactInfo, cardLevel;
    private com.google.android.material.card.MaterialCardView cardRating;
    private RatingBar ratingBar;
    private TextView tvRatingAvg, tvRatingCount, tvNoRating;

    // Data
    private User profileUser;
    private User currentUser;
    private ProfileDonationAdapter donationAdapter;
    private final List<Donation> shownDonations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.scrollView), (v, insets) -> {
                    Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                    return insets;
                });

        bindViews();
        setupRecyclerView();

        currentUser = SharedPreferencesUtil.getUser(this);
        String targetUserId = getIntent().getStringExtra("USER_ID");
        boolean viewingSelf = targetUserId == null
                || (currentUser != null && targetUserId.equals(currentUser.getId()));

        if (viewingSelf) {
            profileUser = currentUser;
            mode = (currentUser != null && currentUser.isAdmin())
                    ? ProfileMode.SELF_ADMIN : ProfileMode.SELF;
            renderProfile();
        } else {
            loadUserFromDb(targetUserId);
        }
    }

    private void bindViews() {
        tvPageTitle         = findViewById(R.id.tvTitle);
        tvUserName          = findViewById(R.id.tvUserName);
        tvFullName          = findViewById(R.id.tvFullName);
        tvLevel             = findViewById(R.id.tvLevel);
        tvPhone             = findViewById(R.id.tvPhone);
        tvEmail             = findViewById(R.id.tvEmail);
        tvBirthday          = findViewById(R.id.tvBirthday);
        tvDonationCount     = findViewById(R.id.tvDonationCount);
        tvAdminState        = findViewById(R.id.tvAdminState);
        ivProfile           = findViewById(R.id.imgProfile);
        ivLevel             = findViewById(R.id.imgMedal);
        btnEdit             = findViewById(R.id.btnEditProfile);
        btnChat             = findViewById(R.id.btnChat);
        btnViewAllDonations = findViewById(R.id.btnViewAllDonations);
        layoutContactInfo   = findViewById(R.id.layoutContactInfo);
        tvContactTitle      = findViewById(R.id.tvContactTitle);
        layoutChatRow       = findViewById(R.id.layoutChatRow);
        layoutDonationsSection = findViewById(R.id.layoutDonationsSection);
        layoutPublicInfo    = findViewById(R.id.layoutPublicInfo);
        cardLevel           = findViewById(R.id.cardLevel);
        rvUserDonations     = findViewById(R.id.rvUserDonations);
        btnLeft             = findViewById(R.id.btnLeft);
        btnRight            = findViewById(R.id.btnRight);
        cardRating          = findViewById(R.id.cardRating);
        ratingBar           = findViewById(R.id.ratingBar);
        tvRatingAvg         = findViewById(R.id.tvRatingAvg);
        tvRatingCount       = findViewById(R.id.tvRatingCount);
        tvNoRating          = findViewById(R.id.tvNoRating);

        btnLeft.setOnClickListener(v  -> rvUserDonations.smoothScrollBy(300, 0));
        btnRight.setOnClickListener(v -> rvUserDonations.smoothScrollBy(-300, 0));
    }

    private void setupRecyclerView() {
        rvUserDonations.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        donationAdapter = new ProfileDonationAdapter(this, shownDonations);
        donationAdapter.setOnItemClickListener(donation -> {
            Intent intent = new Intent(this, DonationDetailActivity.class);
            intent.putExtra("DONATION_ID", donation.getId());
            startActivity(intent);
        });
        rvUserDonations.setAdapter(donationAdapter);
    }

    private void loadUserFromDb(String userId) {
        databaseService.getUserService().get(userId,
                new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        if (user == null) {
                            runOnUiThread(() -> {
                                Toast.makeText(UserProfileActivity.this,
                                        "משתמש לא נמצא", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                            return;
                        }
                        profileUser = user;
                        if (currentUser != null && profileUser.isAdmin() && currentUser.isAdmin())
                            mode = ProfileMode.ADMIN_ADMIN;
                        else if (currentUser != null && currentUser.isAdmin())
                            mode = ProfileMode.OTHER_ADMIN;
                        else
                            mode = ProfileMode.OTHER_USER;

                        runOnUiThread(() -> renderProfile());
                    }
                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(UserProfileActivity.this,
                                    "שגיאה בטעינת פרופיל", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
    }

    private void renderProfile() {
        if (profileUser == null) return;

        tvUserName.setText(profileUser.getUserName());
        tvFullName.setText(profileUser.getFullName());
        tvDonationCount.setText(profileUser.getDonationCounter() + " תרומות");

        String photo = profileUser.getProfilePic();
        if (photo != null && !photo.isEmpty()) {
            ivProfile.setImageBitmap(ImageUtil.fromBase64(photo));
        }

        UserLevel level = UserLevel.fromDonationCount(profileUser.getDonationCounter());

        switch (mode) {

            case SELF:
                showLevelRow(level);
                tvAdminState.setVisibility(View.GONE);
                tvPageTitle.setText("הפרופיל שלי");
                layoutPublicInfo.setVisibility(View.VISIBLE);
                layoutContactInfo.setVisibility(View.VISIBLE);
                tvContactTitle.setVisibility(View.VISIBLE);
                layoutChatRow.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                btnEdit.setOnClickListener(v -> startActivity(
                        new Intent(this, updateDetailsActivity.class)));
                loadDonations(false);
                break;

            case SELF_ADMIN:
                hideLevelRow();
                tvAdminState.setVisibility(View.VISIBLE);
                layoutPublicInfo.setVisibility(View.GONE);
                tvPageTitle.setText("הפרופיל שלי");
                layoutContactInfo.setVisibility(View.VISIBLE);
                tvContactTitle.setVisibility(View.VISIBLE);
                layoutChatRow.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                btnEdit.setOnClickListener(v -> startActivity(
                        new Intent(this, updateDetailsActivity.class)));
                loadDonations(false);
                break;

            case OTHER_USER:
                showLevelRow(level);
                tvAdminState.setVisibility(View.GONE);
                tvPageTitle.setText("פרופיל תורם");
                layoutPublicInfo.setVisibility(View.VISIBLE);
                layoutContactInfo.setVisibility(View.GONE);
                tvContactTitle.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                setupChatButton();
                loadDonations(true);
                break;

            case OTHER_ADMIN:
                showLevelRow(level);
                tvAdminState.setVisibility(View.GONE);
                tvPageTitle.setText("פרופיל משתמש");
                layoutPublicInfo.setVisibility(View.VISIBLE);
                layoutContactInfo.setVisibility(View.VISIBLE);
                tvContactTitle.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                setupAdminChatButton();
                loadDonations(false);
                break;

            case ADMIN_ADMIN:
                hideLevelRow();
                tvAdminState.setVisibility(View.VISIBLE);
                layoutPublicInfo.setVisibility(View.GONE);
                tvPageTitle.setText("פרופיל מנהל");
                layoutContactInfo.setVisibility(View.VISIBLE);
                tvContactTitle.setVisibility(View.VISIBLE);
                layoutChatRow.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                loadDonations(false);
                break;
        }

        loadRating();

        btnViewAllDonations.setVisibility(View.VISIBLE);
        btnViewAllDonations.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDonationsActivity.class);
            intent.putExtra("USER_ID", profileUser.getId());
            intent.putExtra("USER_NAME", profileUser.getFullName());

            String viewMode;
            switch (mode) {
                case SELF:
                case SELF_ADMIN:
                    viewMode = "SELF";
                    break;
                case OTHER_ADMIN:
                case ADMIN_ADMIN:
                    viewMode = "ADMIN";
                    break;
                default: // OTHER_USER
                    viewMode = "OTHER";
                    break;
            }
            intent.putExtra("VIEW_MODE", viewMode);
            startActivity(intent);
        });
    }

    private void showLevelRow(UserLevel level) {
        layoutPublicInfo.setVisibility(View.VISIBLE);
        cardLevel.setVisibility(View.VISIBLE);
        ivLevel.setImageResource(level.getIconRes());
        tvLevel.setText(level.getLabel());
    }

    private void hideLevelRow() {
        cardLevel.setVisibility(View.GONE);
    }

    private void fillContactInfo() {
        tvPhone.setText(profileUser.getPhoneNumber());
        tvEmail.setText(profileUser.getEmail());
        tvBirthday.setText(profileUser.getDateOfBirth());
    }

    private void setupChatButton() {
        if (currentUser == null) {
            layoutChatRow.setVisibility(View.GONE);
            return;
        }

        DatabaseService.getInstance().getChatService()
                .getUserChats(currentUser.getId(),
                        new IDatabaseService.DatabaseCallback<List<Chat>>() {
                            @Override
                            public void onCompleted(List<Chat> chats) {
                                List<Chat> relevantChats = chats.stream()
                                        .filter(c -> profileUser.getId().equals(c.getOtherUserId()))
                                        .collect(Collectors.toList());

                                runOnUiThread(() -> {
                                    if (relevantChats.isEmpty()) {
                                        layoutChatRow.setVisibility(View.GONE);
                                    } else if (relevantChats.size() == 1) {
                                        layoutChatRow.setVisibility(View.VISIBLE);
                                        btnChat.setText("המשך שיחה");
                                        btnChat.setOnClickListener(v ->
                                                openChat(relevantChats.get(0)));
                                    } else {
                                        layoutChatRow.setVisibility(View.VISIBLE);
                                        btnChat.setText("המשך שיחה");
                                        btnChat.setOnClickListener(v ->
                                                showChatPickerDialog(relevantChats));
                                    }
                                });
                            }
                            @Override
                            public void onFailed(Exception e) {
                                runOnUiThread(() -> layoutChatRow.setVisibility(View.GONE));
                            }
                        });
    }

    private void setupAdminChatButton() {
        String chatId = "admin_" + profileUser.getId();
        layoutChatRow.setVisibility(View.VISIBLE);
        btnChat.setText("צ'אט מנהלי");
        btnChat.setOnClickListener(v -> {
            Intent i = new Intent(UserProfileActivity.this, ChatActivity.class);
            i.putExtra("CHAT_ID", chatId);
            i.putExtra("OTHER_USER_NAME", profileUser.getFullName());
            i.putExtra("OTHER_USER_ID", profileUser.getId());
            startActivity(i);
        });
    }

    private void openChat(Chat chat) {
        Intent i = new Intent(UserProfileActivity.this, ChatActivity.class);
        i.putExtra("CHAT_ID", chat.getId());
        i.putExtra("OTHER_USER_NAME", profileUser.getFullName());
        i.putExtra("OTHER_USER_ID", profileUser.getId());
        startActivity(i);
    }

    private void showChatPickerDialog(List<Chat> chats) {
        String[] labels = chats.stream()
                .map(c -> {
                    String name = c.getDonationName();
                    return (name != null && !name.isEmpty())
                            ? "תרומה: " + name
                            : "שיחה (" + c.getId() + ")";
                })
                .toArray(String[]::new);

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("בחר שיחה")
                .setItems(labels, (dialog, which) -> openChat(chats.get(which)))
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void loadDonations(boolean availableOnly) {
        databaseService.getDonationService().getByGiverId(
                profileUser.getId(),
                new IDatabaseService.DatabaseCallback<List<Donation>>() {
                    @Override
                    public void onCompleted(List<Donation> donations) {
                        List<Donation> filtered = availableOnly
                                ? donations.stream()
                                .filter(d -> d.getStatus() == DonationStatus.APPROVED_AVAILABLE)
                                .collect(Collectors.toList())
                                : donations;

                        runOnUiThread(() -> {
                            shownDonations.clear();
                            shownDonations.addAll(filtered);
                            donationAdapter.notifyDataSetChanged();
                            updateDonationsLayout(filtered.size());
                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> updateDonationsLayout(0));
                    }
                });
    }

    private void updateDonationsLayout(int count) {
        View emptyView = findViewById(R.id.emptyDonationsView);
        if (count == 0) {
            rvUserDonations.setVisibility(View.GONE);
            btnLeft.setVisibility(View.GONE);
            btnRight.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
        } else {
            rvUserDonations.setVisibility(View.VISIBLE);
            if (emptyView != null) emptyView.setVisibility(View.GONE);
            if (count < 3) {
                btnLeft.setVisibility(View.GONE);
                btnRight.setVisibility(View.GONE);
            } else {
                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadRating() {
        if (mode == ProfileMode.SELF_ADMIN || mode == ProfileMode.ADMIN_ADMIN) {
            cardRating.setVisibility(View.GONE);
            tvNoRating.setVisibility(View.GONE);
            return;
        }
        android.util.Log.d("RATING_DEBUG", "loadRating called, mode: " + mode
                + ", userId: " + profileUser.getId());

        DatabaseService.getInstance().getRateService()
                .getRatesForUser(profileUser.getId(),
                        new IDatabaseService.DatabaseCallback<List<Rate>>() {
                            @Override
                            public void onCompleted(List<Rate> rates) {
                                android.util.Log.d("RATING_DEBUG", "rates count: "
                                        + (rates == null ? "null" : rates.size()));
                                runOnUiThread(() -> {
                                    if (rates == null || rates.isEmpty()) {
                                        cardRating.setVisibility(View.GONE);
                                        tvNoRating.setVisibility(View.VISIBLE);
                                        return;
                                    }
                                    double avg = rates.stream()
                                            .mapToInt(Rate::getStarAmount)
                                            .average()
                                            .orElse(0);
                                    int count = rates.size();
                                    // מעגל לחצי כוכב הכי קרוב
                                    float roundedAvg = Math.round(avg * 2) / 2.0f;

                                    ratingBar.setRating(roundedAvg);
                                    tvRatingAvg.setText(String.format("%.1f", avg));
                                    tvRatingCount.setText(count + " דירגו");
                                    cardRating.setVisibility(View.VISIBLE);
                                    tvNoRating.setVisibility(View.GONE);
                                });
                            }
                            @Override
                            public void onFailed(Exception e) {
                                android.util.Log.e("RATING_DEBUG", "failed: " + e.getMessage());
                                runOnUiThread(() -> {
                                    cardRating.setVisibility(View.GONE);
                                    tvNoRating.setVisibility(View.GONE);
                                });
                            }
                        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = SharedPreferencesUtil.getUser(this);
        String targetUserId = getIntent().getStringExtra("USER_ID");
        boolean viewingSelf = targetUserId == null
                || (currentUser != null && targetUserId.equals(currentUser.getId()));

        if (viewingSelf) {
            profileUser = currentUser;
            mode = (currentUser != null && currentUser.isAdmin())
                    ? ProfileMode.SELF_ADMIN : ProfileMode.SELF;
            renderProfile();
        } else {
            loadUserFromDb(targetUserId);
        }
    }
}