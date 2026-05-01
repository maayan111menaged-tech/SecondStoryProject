package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private enum ProfileMode { SELF, SELF_ADMIN, OTHER_USER, OTHER_ADMIN, ADMIN_ADMIN }
    private ProfileMode mode;

    // Views
    private TextView   tvPageTitle, tvUserName, tvFullName, tvLevel;
    private TextView   tvPhone, tvEmail, tvBirthday, tvDonationCount, tvAdminState;
    private ImageView  ivProfile, ivLevel;
    private Button     btnEdit, btnChat, btnViewAllDonations;
    private LinearLayout layoutContactInfo, layoutChatRow,
            layoutDonationsSection, layoutPublicInfo, layoutLevelRow;
    private RecyclerView rvUserDonations;
    private android.widget.ImageButton btnLeft, btnRight;

    // Data
    private User profileUser;
    private User currentUser;
    private ProfileDonationAdapter donationAdapter;
    private final List<Donation> shownDonations = new ArrayList<>();

    @Override
    protected int getSelectedBottomNavItem() {
        return (mode == ProfileMode.SELF || mode == ProfileMode.SELF_ADMIN)
                ? R.id.menu_profile : -1;
    }

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
        layoutChatRow       = findViewById(R.id.layoutChatRow);
        layoutDonationsSection = findViewById(R.id.layoutDonationsSection);
        layoutPublicInfo    = findViewById(R.id.layoutPublicInfo);
        layoutLevelRow      = findViewById(R.id.layoutLevelRow);
        rvUserDonations     = findViewById(R.id.rvUserDonations);
        btnLeft             = findViewById(R.id.btnLeft);
        btnRight            = findViewById(R.id.btnRight);

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

        // ── פרטים משותפים לכל המצבים ──
        tvUserName.setText(profileUser.getUserName());
        tvFullName.setText(profileUser.getFullName());
        tvDonationCount.setText(profileUser.getDonationCounter() + " תרומות");

        String photo = profileUser.getProfilePhoneUrl();
        if (photo != null && !photo.isEmpty()) {
            ivProfile.setImageBitmap(ImageUtil.fromBase64(photo));
        }

        UserLevel level = UserLevel.fromDonationCount(profileUser.getDonationCounter());

        switch (mode) {

            case SELF:
                // משתמש רגיל רואה את עצמו – מציג רמה, מסתיר אדמין
                showLevelRow(level);
                tvAdminState.setVisibility(View.GONE);

                tvPageTitle.setText("הפרופיל שלי");
                layoutContactInfo.setVisibility(View.VISIBLE);
                layoutPublicInfo.setVisibility(View.VISIBLE);
                layoutChatRow.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                btnEdit.setOnClickListener(v -> startActivity(
                        new Intent(this, updateDetailsActivity.class)));
                loadDonations(false);
                break;

            case SELF_ADMIN:
                // אדמין רואה את עצמו – מציג ADMIN, מסתיר רמה
                hideLevelRow();
                tvAdminState.setVisibility(View.VISIBLE);

                tvPageTitle.setText("הפרופיל שלי");
                layoutContactInfo.setVisibility(View.VISIBLE);
                layoutPublicInfo.setVisibility(View.VISIBLE);
                layoutChatRow.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                btnEdit.setOnClickListener(v -> startActivity(
                        new Intent(this, updateDetailsActivity.class)));
                loadDonations(false);
                break;

            case OTHER_USER:
                // משתמש רגיל רואה אחר – מציג רמה, מסתיר אדמין
                showLevelRow(level);
                tvAdminState.setVisibility(View.GONE);

                tvPageTitle.setText("פרופיל תורם");
                layoutContactInfo.setVisibility(View.GONE);
                layoutPublicInfo.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                setupChatButton();
                loadDonations(true); // רק זמינות
                break;

            case OTHER_ADMIN:
                // אדמין רואה משתמש רגיל – מציג רמה, מסתיר אדמין
                showLevelRow(level);
                tvAdminState.setVisibility(View.GONE);

                tvPageTitle.setText("פרופיל משתמש");
                layoutContactInfo.setVisibility(View.VISIBLE);
                layoutPublicInfo.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                setupAdminChatButton();
                loadDonations(false); // כל הסטטוסים
                break;

            case ADMIN_ADMIN:
                // אדמין רואה אדמין אחר – מציג ADMIN, מסתיר רמה
                hideLevelRow();
                tvAdminState.setVisibility(View.VISIBLE);

                tvPageTitle.setText("פרופיל מנהל");
                layoutContactInfo.setVisibility(View.VISIBLE);
                layoutPublicInfo.setVisibility(View.VISIBLE);
                layoutChatRow.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                layoutDonationsSection.setVisibility(View.VISIBLE);
                fillContactInfo();
                loadDonations(false); // כל הסטטוסים
                break;
        }

        // כפתור "כל התרומות" – נראה תמיד
        btnViewAllDonations.setVisibility(View.VISIBLE);
        btnViewAllDonations.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDonationsActivity.class);
            intent.putExtra("USER_ID", profileUser.getId());
            intent.putExtra("USER_NAME", profileUser.getFullName());
            boolean fullAccess = mode == ProfileMode.SELF
                    || mode == ProfileMode.OTHER_ADMIN
                    || mode == ProfileMode.ADMIN_ADMIN
                    || mode == ProfileMode.SELF_ADMIN;
            intent.putExtra("FULL_ACCESS", fullAccess);
            startActivity(intent);
        });
    }

    /** מציג את שורת הרמה + מדליה עם הערכים הנכונים */
    private void showLevelRow(UserLevel level) {
        layoutLevelRow.setVisibility(View.VISIBLE);
        ivLevel.setImageResource(level.getIconRes());
        tvLevel.setText(level.getLabel());
    }

    /** מסתיר את שורת הרמה + מדליה */
    private void hideLevelRow() {
        layoutLevelRow.setVisibility(View.GONE);
    }

    private void fillContactInfo() {
        tvPhone.setText(profileUser.getPhoneNumber());
        tvEmail.setText(profileUser.getEmail());
        tvBirthday.setText(profileUser.getDateOfBirth());
    }

    /**
     * מחפש צ'אטים קיימים עם profileUser.
     * 0 צ'אטים  → מסתיר את layoutChatRow לגמרי
     * 1 צ'אט    → פותח ישירות
     * 2+ צ'אטים → מציג דיאלוג בחירה
     */
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
                                // מסננים רק צ'אטים עם המשתמש הנצפה
                                List<Chat> relevantChats = chats.stream()
                                        .filter(c -> profileUser.getId().equals(c.getOtherUserId()))
                                        .collect(Collectors.toList());

                                runOnUiThread(() -> {
                                    if (relevantChats.isEmpty()) {
                                        // אין צ'אט פתוח – מסתירים לגמרי
                                        layoutChatRow.setVisibility(View.GONE);

                                    } else if (relevantChats.size() == 1) {
                                        // צ'אט אחד – פותחים ישירות
                                        layoutChatRow.setVisibility(View.VISIBLE);
                                        btnChat.setText("המשך שיחה");
                                        btnChat.setOnClickListener(v ->
                                                openChat(relevantChats.get(0)));

                                    } else {
                                        // כמה צ'אטים – דיאלוג בחירה
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


    /**
     * דיאלוג בחירה כאשר יש יותר מצ'אט אחד עם אותו משתמש.
     * כל שורה מציגה את שם התרומה המשויכת לצ'אט.
     */
    private void showChatPickerDialog(List<Chat> chats) {
        String[] labels = chats.stream()
                .map(c -> {
                    String name = c.getDonationName();
                    return (name != null && !name.isEmpty())
                            ? "תרומה: " + name
                            : "שיחה (" + c.getId() + ")";
                })
                .toArray(String[]::new);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("בחר שיחה")
                .setItems(labels, (dialog, which) -> openChat(chats.get(which)))
                .setNegativeButton("ביטול", null)
                .show();
    }

    /**
     * @param availableOnly true  = רק APPROVED_AVAILABLE (OTHER_USER)
     *                      false = כל הסטטוסים
     */
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
}