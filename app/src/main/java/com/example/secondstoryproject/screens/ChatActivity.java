package com.example.secondstoryproject.screens;

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

public class ChatActivity extends BaseActivity {

    // ── צבעי באנר ──
    private static final String COLOR_DEFAULT  = "#075E54";
    private static final String COLOR_INACTIVE = "#B71C1C";
    private static final String COLOR_TAKEN    = "#E65100";
    private static final String COLOR_MATCH    = "#F57F17";

    public enum HeaderState { DEFAULT, INACTIVE, DELETED, TAKEN, MATCH }

    // ── Views ──
    private LinearLayout   layoutChatHeader;
    private LinearLayout   layoutStatusRow;
    private LinearLayout   layoutMatchBanner;
    private ImageButton    btnBack;
    private ImageView      ivChatAvatar;
    private TextView       tvChatTitle;
    private TextView       tvStatusIcon;
    private TextView       tvChatStatus;
    private TextView       tvMatchSubtitle;
    private MaterialButton btnDeleteChat;
    private MaterialButton btnMatch;

    private RecyclerView   rvMessages;
    private EditText       etMessage;
    private MaterialButton btnSend;
    private MessageAdapter messageAdapter;
    private ValueEventListener messagesListener;

    // ── Data ──
    private String  chatId;
    private String  currentUserId;
    private boolean currentUserIsAdmin;
    private String  donationId;
    private String  donationGiverId;
    private String  otherUserId;
    private String  otherUserName;

    // ─────────────────────────────────────────────────────────
    //  onCreate
    // ─────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId        = getIntent().getStringExtra("CHAT_ID");
        otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");
        otherUserId   = getIntent().getStringExtra("OTHER_USER_ID");

        User currentUser = SharedPreferencesUtil.getUser(this);
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
    }

    // ─────────────────────────────────────────────────────────
    //  Bind Views
    // ─────────────────────────────────────────────────────────
    private void bindViews() {
        layoutChatHeader  = findViewById(R.id.layout_chat_header);
        layoutStatusRow   = findViewById(R.id.layout_status_row);
        layoutMatchBanner = findViewById(R.id.layout_match_banner);
        btnBack           = findViewById(R.id.btn_back);
        ivChatAvatar      = findViewById(R.id.iv_chat_avatar);
        tvChatTitle       = findViewById(R.id.tv_chat_title);
        tvStatusIcon      = findViewById(R.id.tv_status_icon);
        tvChatStatus      = findViewById(R.id.tv_chat_status);
        tvMatchSubtitle   = findViewById(R.id.tv_match_subtitle);
        btnDeleteChat     = findViewById(R.id.btn_delete_chat);
        btnMatch          = findViewById(R.id.btn_match);

        rvMessages = findViewById(R.id.rv_messages);
        etMessage  = findViewById(R.id.et_message);
        btnSend    = findViewById(R.id.btn_send);

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

    // ─────────────────────────────────────────────────────────
    //  setHeaderState
    // ─────────────────────────────────────────────────────────
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

        // שורת סטטוס
        if (statusMessage != null && !statusMessage.isEmpty()) {
            layoutStatusRow.setVisibility(View.VISIBLE);
            tvStatusIcon.setText(icon);
            tvChatStatus.setText(statusMessage);
        } else {
            layoutStatusRow.setVisibility(View.GONE);
        }

        // חסימת שליחה
        boolean blocked = (state == HeaderState.INACTIVE
                || state == HeaderState.DELETED
                || state == HeaderState.TAKEN);
        etMessage.setEnabled(!blocked);
        etMessage.setAlpha(blocked ? 0.5f : 1f);
        btnSend.setEnabled(!blocked);
        btnSend.setAlpha(blocked ? 0.5f : 1f);
        if (blocked) etMessage.setHint("לא ניתן לשלוח הודעות");

        // כפתור מחיקה
        btnDeleteChat.setVisibility(
                state == HeaderState.DELETED ? View.VISIBLE : View.GONE);

        // באנר MATCH
        layoutMatchBanner.setVisibility(
                state == HeaderState.MATCH ? View.VISIBLE : View.GONE);
    }

    // ─────────────────────────────────────────────────────────
    //  Load avatar
    // ─────────────────────────────────────────────────────────
    private void loadOtherUserAvatar(String userId) {
        if (userId == null || userId.equals("admin")) return;
        DatabaseService.getInstance().getUserService()
                .get(userId, new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        if (user == null) return;
                        runOnUiThread(() -> {
                            String photo = user.getProfilePhoneUrl();
                            if (photo != null && !photo.isEmpty()) {
                                ivChatAvatar.setImageBitmap(ImageUtil.fromBase64(photo));
                            }
                        });
                    }
                    @Override public void onFailed(Exception e) {}
                });
    }

    // ─────────────────────────────────────────────────────────
    //  checkChatStatus
    // ─────────────────────────────────────────────────────────
    private void checkChatStatus(String otherUserIdFromIntent) {
        FirebaseDatabase.getInstance(
                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("chats")
                .child(chatId)
                .child("metadata")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        listenToMessages(); // ← גם במקרה כשל
                        return;
                    }

                    DataSnapshot meta = task.getResult();

                    String type    = meta.child("type").getValue(String.class);
                    String giverId = meta.child("giverId").getValue(String.class);
                    donationGiverId = giverId;
                    messageAdapter.setDonationGiverId(donationGiverId);
                    donationId = meta.child("donationId").getValue(String.class);
                    messageAdapter.setDonationId(donationId);

                    Boolean isDeleted = meta.child("donorDeleted").getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isDeleted)) {
                        runOnUiThread(this::showDeletedUserHeader);
                        listenToMessages(); // ← donationGiverId כבר מוגדר
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
                        DatabaseService.getInstance().getUserService()
                                .get(finalOtherUserId, new IDatabaseService.DatabaseCallback<User>() {
                                    @Override
                                    public void onCompleted(User otherUser) {
                                        if (otherUser == null) {
                                            runOnUiThread(ChatActivity.this::showDeletedUserHeader);
                                            listenToMessages(); // ← donationGiverId כבר מוגדר
                                            return;
                                        }
                                        if (!otherUser.isActive()) {
                                            runOnUiThread(ChatActivity.this::showInactiveHeader);
                                            listenToMessages(); // ← donationGiverId כבר מוגדר
                                            return;
                                        }
                                        if ("donation".equals(type) && donationId != null) {
                                            checkDonationMatchStatus(giverId, finalOtherUserId);
                                        } else {
                                            listenToMessages();
                                        }
                                    }
                                    @Override public void onFailed(Exception e) {
                                        listenToMessages();
                                    }
                                });
                    } else if ("donation".equals(type) && donationId != null) {
                        checkDonationMatchStatus(giverId, finalOtherUserId);
                    } else {
                        listenToMessages();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────
    //  checkDonationMatchStatus
    // ─────────────────────────────────────────────────────────
    private void checkDonationMatchStatus(String giverId, String otherUserId) {
        FirebaseDatabase.getInstance(
                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("donations")
                .child(donationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        listenToMessages();
                        return;
                    }

                    String receiverID = task.getResult()
                            .child("receiverID").getValue(String.class);

                    runOnUiThread(() -> {
                        if (receiverID == null || receiverID.isEmpty()) {
                            if (currentUserId.equals(giverId) && otherUserId != null) {
                                showMatchHeader(otherUserId);
                            }
                        } else if (currentUserId.equals(giverId)) {
                            if (!receiverID.equals(otherUserId)) {
                                showDonationTakenHeader();
                            }
                        } else if (!currentUserId.equals(receiverID)) {
                            showDonationTakenHeader();
                        }
                        listenToMessages(); // ← תמיד בסוף
                    });
                });
    }
    // ─────────────────────────────────────────────────────────
    //  Header state helpers
    // ─────────────────────────────────────────────────────────
    private void showInactiveHeader() {
        setHeaderState(HeaderState.INACTIVE, "משתמש זה אינו פעיל");
    }

    private void showDeletedUserHeader() {
        setHeaderState(HeaderState.DELETED, "משתמש זה הוסר מהמערכת");
        btnDeleteChat.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("מחיקת שיחה")
                        .setMessage("האם למחוק את השיחה לצמיתות?")
                        .setPositiveButton("מחק", (d, w) ->
                                DatabaseService.getInstance().getChatService()
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

    private void showMatchHeader(String matchCandidateUserId) {
        String name = getIntent().getStringExtra("OTHER_USER_NAME");
        setHeaderState(HeaderState.MATCH, "תרומה ממתינה לאישור");

        tvMatchSubtitle.setText("לחץ ✓ Match כדי לאשר את "
                + (name != null ? name : "המשתמש"));

        btnMatch.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("אישור תרומה")
                        .setMessage("לאשר את " + (name != null ? name : "המשתמש")
                                + " כמקבל התרומה?")
                        .setPositiveButton("כן, Match!", (d, w) -> performMatch(matchCandidateUserId))
                        .setNegativeButton("ביטול", null)
                        .show()
        );
    }

    // ─────────────────────────────────────────────────────────
    //  performMatch
    // ─────────────────────────────────────────────────────────
    private void performMatch(String matchedUserId) {
        DatabaseService.getInstance().getChatService()
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
        DatabaseService.getInstance().getUserService()
                .incrementDonationCounter(currentUserId, null);
    }

    // ─────────────────────────────────────────────────────────
    //  sendSystemMessage
    // ─────────────────────────────────────────────────────────
    private void sendSystemMessage(String text, boolean isRatingRequest) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance(
                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("chats").child(chatId).child("messages");

        String messageId = messagesRef.push().getKey();
        Message msg = new Message(messageId, "system", text, System.currentTimeMillis());
        msg.setRatingRequest(isRatingRequest);
        messagesRef.child(messageId).setValue(msg)
                .addOnSuccessListener(unused ->
                        FirebaseDatabase.getInstance(
                                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("chats")
                                .child(chatId)
                                .child("metadata")
                                .child("receiverId")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult().exists()) {
                                        String receiverId = task.getResult().getValue(String.class);
                                        if (receiverId != null) {
                                            DatabaseService.getInstance()
                                                    .getChatService()
                                                    .incrementUnread(chatId, receiverId);
                                        }
                                    }
                                }));
    }

    // ─────────────────────────────────────────────────────────
    //  setupRatingListener
    // ─────────────────────────────────────────────────────────
    private void setupRatingListener() {
        messageAdapter.setOnRateListener((stars, comment) -> {
            Rate rate = new Rate(
                    donationGiverId,
                    currentUserId,
                    donationId,
                    stars,
                    comment
            );
            DatabaseService.getInstance()
                    .getRateService()
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

    // ─────────────────────────────────────────────────────────
    //  listenToMessages
    // ─────────────────────────────────────────────────────────
    private void listenToMessages() {
        String senderId = currentUserIsAdmin ? "admin" : currentUserId;
        DatabaseService.getInstance().getChatService().resetUnread(chatId, senderId);

        messagesListener = DatabaseService.getInstance()
                .getChatService()
                .listenToMessages(chatId, new IDatabaseService.DatabaseCallback<List<Message>>() {
                    @Override
                    public void onCompleted(List<Message> messages) {
                        runOnUiThread(() -> {
                            messageAdapter.setMessages(messages);
                            if (!messages.isEmpty()) {
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

    // ─────────────────────────────────────────────────────────
    //  sendMessage
    // ─────────────────────────────────────────────────────────
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        etMessage.setText("");

        DatabaseService.getInstance().getChatService()
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

    // ─────────────────────────────────────────────────────────
    //  onDestroy
    // ─────────────────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null && chatId != null) {
            DatabaseService.getInstance().getChatService()
                    .removeListener(chatId, messagesListener);
        }
    }
}