package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.MessageAdapter;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.Message;
import com.example.secondstoryproject.models.Rate;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

// Chat screen — handles messaging between two users or between a user and admin.
// Displays status banners for edge cases: inactive user, deleted user, taken donation, canceled donation, match
public class ChatActivity extends BaseActivity {

    // banner colors for each header state
    private static final String COLOR_DEFAULT  = "#33083f"; // dark_purple — normal chat
    private static final String COLOR_INACTIVE = "#B71C1C"; // red — blocked or deleted user
    private static final String COLOR_TAKEN    = "#E65100"; // orange — donation taken or canceled
    private static final String COLOR_MATCH    = "#F57F17"; // yellow — waiting for giver to confirm

    // represents the current visual state of the chat header
    public enum HeaderState { DEFAULT, INACTIVE, DELETED, TAKEN, MATCH }

    // ── Views ──
    private LinearLayout   layoutChatHeader;
    private LinearLayout   layoutStatusRow;
    private LinearLayout   layoutMatchBanner;
    private ImageButton    btnBack;
    private ImageView      ivChatAvatar;
    private TextView       tvChatTitle;
    // shows the donation name in the header — tappable to open donation details
    private TextView tvHeaderDonationName;
    private TextView       tvStatusIcon;
    private TextView       tvChatStatus;
    private TextView       tvMatchSubtitle;
    // shown only when the other user was deleted — allows deleting the chat
    private MaterialButton btnDeleteChat;
    // shown only when the giver can confirm a match
    private MaterialButton btnMatch;

    private RecyclerView   rvMessages;
    private EditText       etMessage;
    private MaterialButton btnSend;
    private MessageAdapter messageAdapter;
    private ValueEventListener messagesListener;

    private String  chatId;
    private String  currentUserId;
    private boolean currentUserIsAdmin;
    private String  donationId;
    private String  donationGiverId;
    private String  otherUserId;
    private String  otherUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId        = getIntent().getStringExtra("CHAT_ID");
        otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");
        otherUserId   = getIntent().getStringExtra("OTHER_USER_ID");

        User currentUser = SharedPreferencesUtil.getUser(this);
        // if there's no logged-in user or no chat ID — close the screen immediately
        if (currentUser == null || chatId == null) {
            finish();
            return;
        }

        currentUserId      = currentUser.getId();
        currentUserIsAdmin = currentUser.isAdmin();

        bindViews();
        setupSendButton();
        setupBackButton();
        setupRatingListener();

        tvChatTitle.setText(otherUserName != null ? otherUserName : "שיחה");
        android.util.Log.d("ChatDebug", "otherUserId=" + otherUserId);

        loadOtherUserAvatar(otherUserId);
        checkChatStatus(otherUserId);

        // tapping the header navigates to the other user's profile
        layoutChatHeader.setOnClickListener(v -> {
            if (chatId != null && chatId.startsWith("admin_")) {
                if (currentUserIsAdmin) {
                    // admin chat — extract the user ID from the chat ID
                    String userId = chatId.replace("admin_", "");
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                } return;
            }
            if (otherUserId != null) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("USER_ID", otherUserId);
                startActivity(intent);
            }
        });
    }

    // connects all XML views to their Java variables
    private void bindViews() {
        layoutChatHeader  = findViewById(R.id.layout_chat_header);
        layoutStatusRow   = findViewById(R.id.layout_status_row);
        layoutMatchBanner = findViewById(R.id.layout_match_banner);
        btnBack           = findViewById(R.id.btn_back);
        ivChatAvatar      = findViewById(R.id.iv_chat_avatar);
        tvChatTitle       = findViewById(R.id.tv_chat_title);
        tvHeaderDonationName = findViewById(R.id.tv_header_donation_name);
        tvStatusIcon      = findViewById(R.id.tv_status_icon);
        tvChatStatus      = findViewById(R.id.tv_chat_status);
        tvMatchSubtitle   = findViewById(R.id.tv_match_subtitle);
        btnDeleteChat     = findViewById(R.id.btn_delete_chat);
        btnMatch          = findViewById(R.id.btn_match);

        rvMessages = findViewById(R.id.rv_messages);
        etMessage  = findViewById(R.id.et_message);
        btnSend    = findViewById(R.id.btn_send);

        // stackFromEnd keeps the latest messages visible at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(currentUserId, currentUserIsAdmin);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    // updates the header color, status message, send block, and action buttons based on state
    private void setHeaderState(HeaderState state, String statusMessage) {
        String color;
        String icon;
        switch (state) {
            case INACTIVE:
                color = COLOR_INACTIVE;
                icon  = "⚠️";
                break;
            case DELETED:
                color = COLOR_INACTIVE;
                icon  = "🚫";
                break;
            case TAKEN:
                color = COLOR_TAKEN;
                icon  = "🔒";
                break;
            case MATCH:
                color = COLOR_MATCH;
                icon  = "🎁";
                break;
            default:
                color = COLOR_DEFAULT;
                icon  = "";
                break;
        }

        layoutChatHeader.setBackgroundColor(Color.parseColor(color));

        // shows the status row only when there is a message to display
        if (statusMessage != null && !statusMessage.isEmpty()) {
            layoutStatusRow.setVisibility(View.VISIBLE);
            tvStatusIcon.setText(icon);
            tvChatStatus.setText(statusMessage);
        } else {
            layoutStatusRow.setVisibility(View.GONE);
        }

        // blocks sending when the chat is no longer active
        boolean blocked = (state == HeaderState.INACTIVE
                || state == HeaderState.DELETED
                || state == HeaderState.TAKEN);
        etMessage.setEnabled(!blocked);
        etMessage.setAlpha(blocked ? 0.5f : 1f);
        btnSend.setEnabled(!blocked);
        btnSend.setAlpha(blocked ? 0.5f : 1f);
        if (blocked) etMessage.setHint("לא ניתן לשלוח הודעות");

        // delete button only shown when the other user was removed from the system
        btnDeleteChat.setVisibility(
                state == HeaderState.DELETED ? View.VISIBLE : View.GONE);

        // match banner only shown when the giver can confirm a receiver
        layoutMatchBanner.setVisibility(
                state == HeaderState.MATCH ? View.VISIBLE : View.GONE);
    }

    // fetches and displays the other user's profile picture in the header
    private void loadOtherUserAvatar(String userId) {
        if (userId == null || userId.equals("admin")) return;
       databaseService.getUserService()
                .get(userId, new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        if (user == null) return;
                        runOnUiThread(() -> {
                            String photo = user.getProfilePic();
                            if (photo != null && !photo.isEmpty()) {
                                ivChatAvatar.setImageBitmap(ImageUtil.fromBase64(photo));
                            }
                        });
                    }
                    @Override public void onFailed(Exception e) {}
                });
    }

    // fetches chat metadata from Firebase and determines which header state to show
    private void checkChatStatus(String otherUserIdFromIntent) {
        databaseService.getChatService()
                .getChatMetadata(chatId, new IDatabaseService.DatabaseCallback<DataSnapshot>() {
                    @Override
                    public void onCompleted(DataSnapshot meta) {
                        if (meta == null || !meta.exists()) {
                            listenToMessages();
                            return;
                        }

                        String type    = meta.child("type").getValue(String.class);
                        String giverId = meta.child("giverId").getValue(String.class);
                        donationGiverId = giverId;
                        messageAdapter.setDonationGiverId(donationGiverId);
                        donationId = meta.child("donationId").getValue(String.class);
                        messageAdapter.setDonationId(donationId);

                        Boolean isDeleted = meta.child("donorDeleted").getValue(Boolean.class);

                        if (donationId != null && !"admin".equals(type)) {
                            databaseService.getDonationService().get(donationId,
                                    new IDatabaseService.DatabaseCallback<Donation>() {
                                        @Override
                                        public void onCompleted(Donation donation) {
                                            if (donation == null) return;
                                            runOnUiThread(() -> {
                                                String donName = donation.getName();
                                                if (donName != null && !donName.isEmpty()) {
                                                    tvHeaderDonationName.setText("📦 " + donName);
                                                    tvHeaderDonationName.setVisibility(View.VISIBLE);
                                                    tvHeaderDonationName.setOnClickListener(v -> {
                                                        Intent intent = new Intent(ChatActivity.this,
                                                                DonationDetailActivity.class);
                                                        intent.putExtra("DONATION_ID", donationId);
                                                        startActivity(intent);
                                                    });
                                                }
                                            });
                                        }
                                        @Override public void onFailed(Exception e) {}
                                    });
                        }

                        if (Boolean.TRUE.equals(isDeleted)) {
                            runOnUiThread(ChatActivity.this::showDeletedUserHeader);
                            listenToMessages();
                            return;
                        }

                        String resolvedOtherUserId = otherUserIdFromIntent;
                        if (resolvedOtherUserId == null) {
                            String metaReceiverId = meta.child("receiverId").getValue(String.class);
                            if ("admin".equals(type)) {
                                resolvedOtherUserId = currentUserIsAdmin ? metaReceiverId : null;
                            } else {
                                resolvedOtherUserId = currentUserId.equals(giverId)
                                        ? metaReceiverId : giverId;
                            }
                        }

                        final String finalOtherUserId = resolvedOtherUserId;

                        if (finalOtherUserId != null && !"admin".equals(finalOtherUserId)) {
                            databaseService.getUserService()
                                    .get(finalOtherUserId, new IDatabaseService.DatabaseCallback<User>() {
                                        @Override
                                        public void onCompleted(User otherUser) {
                                            if (otherUser == null) {
                                                runOnUiThread(ChatActivity.this::showDeletedUserHeader);
                                                listenToMessages();
                                                return;
                                            }
                                            if (!otherUser.isActive()) {
                                                runOnUiThread(ChatActivity.this::showInactiveHeader);
                                                listenToMessages();
                                                return;
                                            }
                                            if ("donation".equals(type) && donationId != null) {
                                                databaseService.getDonationService().get(donationId,
                                                        new IDatabaseService.DatabaseCallback<Donation>() {
                                                            @Override
                                                            public void onCompleted(Donation donation) {
                                                                if (donation != null &&
                                                                        donation.getStatus() == DonationStatus.CANCELLED) {
                                                                    runOnUiThread(() -> {
                                                                        boolean isGiver = currentUserId.equals(giverId);
                                                                        setHeaderState(HeaderState.TAKEN,
                                                                                isGiver ? "ביטלת תרומה זו"
                                                                                        : "התרומה בוטלה על ידי התורם");
                                                                    });
                                                                    listenToMessages();
                                                                    return;
                                                                }
                                                                checkDonationMatchStatus(giverId, finalOtherUserId);
                                                            }
                                                            @Override
                                                            public void onFailed(Exception e) {
                                                                checkDonationMatchStatus(giverId, finalOtherUserId);
                                                            }
                                                        });
                                            } else {
                                                listenToMessages();
                                            }
                                        }
                                        @Override public void onFailed(Exception e) {
                                            listenToMessages();
                                        }
                                    });
                        } else if ("donation".equals(type) && donationId != null) {
                            databaseService.getDonationService().get(donationId,
                                    new IDatabaseService.DatabaseCallback<Donation>() {
                                        @Override
                                        public void onCompleted(Donation donation) {
                                            if (donation != null &&
                                                    donation.getStatus() == DonationStatus.CANCELLED) {
                                                runOnUiThread(() -> {
                                                    boolean isGiver = currentUserId.equals(giverId);
                                                    setHeaderState(HeaderState.TAKEN,
                                                            isGiver ? "ביטלת תרומה זו"
                                                                    : "התרומה בוטלה על ידי התורם");
                                                });
                                                listenToMessages();
                                                return;
                                            }
                                            checkDonationMatchStatus(giverId, finalOtherUserId);
                                        }
                                        @Override
                                        public void onFailed(Exception e) {
                                            checkDonationMatchStatus(giverId, finalOtherUserId);
                                        }
                                    });
                        } else {
                            listenToMessages();
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        listenToMessages();
                    }
                });
    }

    // checks the donation's receiver status to determine if a match banner should be shown
    private void checkDonationMatchStatus(String giverId, String otherUserId) {
        databaseService.getDonationService().get(donationId,
                new IDatabaseService.DatabaseCallback<Donation>() {
                    @Override
                    public void onCompleted(Donation donation) {
                        if (donation == null) {
                            listenToMessages();
                            return;
                        }
                        String receiverID = donation.getReceiverID();
                        runOnUiThread(() -> {
                            if (receiverID == null || receiverID.isEmpty()) {
                                // no receiver yet — giver can confirm this user as a match
                                if (currentUserId.equals(giverId) && otherUserId != null) {
                                    showMatchHeader(otherUserId);
                                }
                            } else if (currentUserId.equals(giverId)) {
                                // giver is viewing a chat with someone who was NOT chosen
                                if (!receiverID.equals(otherUserId)) {
                                    showDonationTakenHeader();
                                }
                            } else if (!currentUserId.equals(receiverID)) {
                                // this user was not chosen as the receiver
                                showDonationTakenHeader();
                            }
                            listenToMessages();
                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        listenToMessages();
                    }
                });
    }

    private void showInactiveHeader() {
        setHeaderState(HeaderState.INACTIVE, "משתמש זה אינו פעיל");
    }

    // shows the deleted header and sets up the delete chat button
    private void showDeletedUserHeader() {
        setHeaderState(HeaderState.DELETED, "משתמש זה הוסר מהמערכת");
        btnDeleteChat.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this ,R.style.DialogTheme)
                        .setTitle("מחיקת שיחה")
                        .setMessage("האם למחוק את השיחה לצמיתות?")
                        .setPositiveButton("מחק", (d, w) ->
                                databaseService.getChatService()
                                        .deleteChat(chatId, currentUserId,
                                                new IDatabaseService.DatabaseCallback<Void>() {
                                                    @Override
                                                    public void onCompleted(Void unused) {
                                                        Toast.makeText(ChatActivity.this,
                                                                "השיחה נמחקה", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                    @Override
                                                    public void onFailed(Exception e) {
                                                        Toast.makeText(ChatActivity.this,
                                                                "שגיאה במחיקת השיחה", Toast.LENGTH_SHORT).show();
                                                    }
                                                }))
                        .setNegativeButton("ביטול", null)
                        .show()
        );
    }

    private void showDonationTakenHeader() {
        setHeaderState(HeaderState.TAKEN, "התרומה כבר נתפסה על ידי מישהו אחר");
    }

    // shows the match banner and sets up the confirm button for the giver
    private void showMatchHeader(String matchCandidateUserId) {
        String name = getIntent().getStringExtra("OTHER_USER_NAME");
        setHeaderState(HeaderState.MATCH, "תרומה ממתינה לאישור");

        tvMatchSubtitle.setText("לחץ ✓ Match כדי לאשר את "
                + (name != null ? name : "המשתמש"));

        btnMatch.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                        .setTitle("אישור תרומה")
                        .setMessage("לאשר את " + (name != null ? name : "המשתמש")
                                + " כמקבל התרומה?")
                        .setPositiveButton("כן, Match!", (d, w) -> performMatch(matchCandidateUserId))
                        .setNegativeButton("ביטול", null)
                        .show()
        );
    }

    // sets the matched receiver in the DB, sends a system message, and resets the header
    private void performMatch(String matchedUserId) {
        databaseService.getChatService()
                .setMatch(donationId, matchedUserId,
                        new IDatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                sendSystemMessage(
                                        "🎉 כל הכבוד! התרומה אושרה.\nתאמו ביניכם את פרטי האיסוף 📦",
                                        true
                                );
                                runOnUiThread(() -> {
                                    setHeaderState(HeaderState.DEFAULT, null);
                                    Toast.makeText(ChatActivity.this,
                                            "התרומה אושרה!", Toast.LENGTH_SHORT).show();
                                });
                            }
                            @Override
                            public void onFailed(Exception e) {
                                runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                                        "שגיאה באישור", Toast.LENGTH_SHORT).show());
                            }
                        });
        // increments the giver's donation counter after a successful match
        databaseService.getUserService()
                .incrementDonationCounter(currentUserId, null);
    }

    // delegates system message sending to the chat service
    private void sendSystemMessage(String text, boolean isRatingRequest) {
        databaseService.getChatService()
                .sendSystemMessage(chatId, text, isRatingRequest,
                        new IDatabaseService.DatabaseCallback<Void>() {
                            @Override public void onCompleted(Void unused) {}
                            @Override public void onFailed(Exception e) {}
                        });
    }

    // listens for rating submissions from the message adapter and saves them to the DB
    private void setupRatingListener() {
        messageAdapter.setOnRateListener((stars, comment) -> {
            Rate rate = new Rate(
                    donationGiverId, currentUserId, donationId, stars, comment );
            databaseService.getRateService()
                    .saveRate(rate, new IDatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void unused) {
                            Toast.makeText(ChatActivity.this,
                                    "תודה על הדירוג! ⭐", Toast.LENGTH_SHORT).show();
                            runOnUiThread(() -> messageAdapter.notifyDataSetChanged());
                        }
                        @Override public void onFailed(Exception e) {}
                    });
        });
    }

    // attaches a real-time listener to the chat's messages and resets the unread count
    private void listenToMessages() {
        String senderId = currentUserIsAdmin ? "admin" : currentUserId;
        databaseService.getChatService().resetUnread(chatId, senderId);

        messagesListener = databaseService.getChatService()
                .listenToMessages(chatId, new IDatabaseService.DatabaseCallback<List<Message>>() {
                    @Override
                    public void onCompleted(List<Message> messages) {
                        runOnUiThread(() -> {
                            messageAdapter.setMessages(messages);
                            if (!messages.isEmpty()) {
                                // scrolls to the latest message automatically
                                rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                            }
                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(ChatActivity.this,
                                        "שגיאה בטעינת הודעות", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // sends a new message from the current user to the chat
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        etMessage.setText("");

        databaseService.getChatService()
                .sendMessage(chatId, currentUserId, text, currentUserIsAdmin,
                        new IDatabaseService.DatabaseCallback<Void>() {
                            @Override public void onCompleted(Void unused) {}
                            @Override
                            public void onFailed(Exception e) {
                                runOnUiThread(() ->
                                        Toast.makeText(ChatActivity.this,
                                                "שגיאה בשליחה", Toast.LENGTH_SHORT).show());
                            }
                        });
    }

    // detaches the messages listener when the activity is destroyed to prevent memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null && chatId != null) {
            databaseService.getChatService()
                    .removeListener(chatId, messagesListener);
        }
    }
}