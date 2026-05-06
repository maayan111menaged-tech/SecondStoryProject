package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatActivity extends BaseActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
    private MaterialButton btnDeleteChat;
    private MessageAdapter messageAdapter;
    private ValueEventListener messagesListener;

    private String chatId;
    private String currentUserId;
    private boolean currentUserIsAdmin;

    // באנר דינמי
    private LinearLayout layoutBanner;
    private TextView tvBannerMessage;

    private LinearLayout layoutMatchBanner;
    private MaterialButton btnMatch;
    private TextView tvMatchSubtitle;
    private String donationId;
    private String donationGiverId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("CHAT_ID");
        String otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");
        String otherUserId = getIntent().getStringExtra("OTHER_USER_ID");

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null || chatId == null) {
            finish();
            return;
        }

        currentUserId = currentUser.getId();
        currentUserIsAdmin = currentUser.isAdmin();

        TextView tvTitle = findViewById(R.id.tv_chat_title);
        tvTitle.setText(otherUserName != null ? "שיחה עם " + otherUserName : "שיחה");

        rvMessages = findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(currentUserId,currentUserIsAdmin);
        rvMessages.setAdapter(messageAdapter);

        layoutBanner = findViewById(R.id.layout_user_status_banner);
        tvBannerMessage = findViewById(R.id.tv_banner_message);
        btnDeleteChat = findViewById(R.id.btn_delete_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendMessage());

        layoutMatchBanner = findViewById(R.id.layout_match_banner);
        btnMatch = findViewById(R.id.btn_match);
        tvMatchSubtitle = findViewById(R.id.tv_match_subtitle);


        messageAdapter.setOnRateListener((stars, comment) -> {
            Rate rate = new Rate(
                    donationGiverId,  // ratedUserId — התורם שמקבל את הדירוג
                    currentUserId,    // ratingUserId — המקבל שנותן את הדירוג
                    donationId,
                    stars,
                    comment
            );
            DatabaseService.getInstance()
                    .getRateService()
                    .saveRate(rate, new IDatabaseService.DatabaseCallback<Void>() {
                        @Override public void onCompleted(Void unused) {
                            Toast.makeText(ChatActivity.this,
                                    "תודה על הדירוג! ⭐", Toast.LENGTH_SHORT).show();
                            runOnUiThread(() -> messageAdapter.notifyDataSetChanged());
                        }
                        @Override public void onFailed(Exception e) {}
                    });
        });

        listenToMessages();

        // ✅ בדיקת מצב הצד השני – נמחק או לא פעיל
        checkChatStatus(otherUserId);


    }

    private void checkChatStatus(String otherUserIdFromIntent) {
        FirebaseDatabase.getInstance(
                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("chats")
                .child(chatId)
                .child("metadata")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) return;

                    DataSnapshot meta = task.getResult();

                    // שלב 1: donorDeleted
                    Boolean isDeleted = meta.child("donorDeleted").getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isDeleted)) {
                        runOnUiThread(this::showDeletedUserBanner);
                        return;
                    }

                    String type    = meta.child("type").getValue(String.class);
                    String giverId = meta.child("giverId").getValue(String.class);
                    donationGiverId = giverId;
                    messageAdapter.setDonationGiverId(donationGiverId);
                    donationId     = meta.child("donationId").getValue(String.class);
                    messageAdapter.setDonationId(donationId);


                    // שלב 2: בדיקת isActive של הצד השני (קוד קיים שלך)
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
                                        if (otherUser != null && !otherUser.isActive()) {
                                            runOnUiThread(ChatActivity.this::showInactiveBanner);
                                            return;
                                        }
                                        // שלב 3: בדיקת match רק בשיחות donation
                                        if ("donation".equals(type) && donationId != null) {
                                            checkDonationMatchStatus(giverId, finalOtherUserId);
                                        }
                                    }
                                    @Override
                                    public void onFailed(Exception e) {}
                                });
                    } else if ("donation".equals(type) && donationId != null) {
                        checkDonationMatchStatus(giverId, finalOtherUserId);
                    }

                });
    }

    private void checkDonationMatchStatus(String giverId, String otherUserId) {
        FirebaseDatabase.getInstance(
                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("donations")
                .child(donationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) return;

                    String receiverID = task.getResult()
                            .child("receiverID").getValue(String.class);

                    runOnUiThread(() -> {
                        if (receiverID == null || receiverID.isEmpty()) {
                            // תרומה פנויה – כפתור Match רק לתורם
                            if (currentUserId.equals(giverId) && otherUserId != null) {
                                showMatchBanner(otherUserId);
                            }
                        } else if (currentUserId.equals(giverId)) {
                            // אני התורם — בדוק אם הצד השני הוא המקבל שנבחר
                            if (!receiverID.equals(otherUserId)) {
                                showDonationTakenBanner();
                            }
                        } else if (!currentUserId.equals(receiverID)) {
                            // ✅ אני לא התורם ולא המקבל הנבחר — התרומה נתפסה על ידי מישהו אחר
                            showDonationTakenBanner();
                        }
                        // אם אני המקבל הנבחר — הכל תקין, לא מראים כלום
                    });
                });
    }

    private void showMatchBanner(String otherUserId) {
        layoutMatchBanner.setVisibility(View.VISIBLE);
        String name = getIntent().getStringExtra("OTHER_USER_NAME");
        tvMatchSubtitle.setText(name != null ? "לחץ Match כדי לאשר את " + name : "לחץ Match כדי לאשר");

        btnMatch.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("אישור תרומה")
                        .setMessage("לאשר את " + name + " כמקבל התרומה?")
                        .setPositiveButton("כן, Match!", (d, w) -> performMatch(otherUserId))
                        .setNegativeButton("ביטול", null)
                        .show()
        );
    }

    private void performMatch(String matchedUserId) {
        DatabaseService.getInstance().getChatService()
                .setMatch(donationId, matchedUserId,
                        new IDatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                sendSystemMessage(
                                        "🎉 כל הכבוד! התרומה אושרה.\nתאמו ביניכם את פרטי האיסוף 📦",
                                        true  // ✅ הכפתור יופיע על ההודעה הזו למקבל
                                );

                                runOnUiThread(() -> {
                                    layoutMatchBanner.setVisibility(View.GONE);
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
                .incrementDonationCounter(currentUserId, null); // null כי לא צריך callback
    }

    private void sendSystemMessage(String text, boolean isRatingRequest) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance(
                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("chats").child(chatId).child("messages");

        String messageId = messagesRef.push().getKey();
        Message msg = new Message(messageId, "system", text, System.currentTimeMillis());
        msg.setRatingRequest(isRatingRequest);
        messagesRef.child(messageId).setValue(msg)
                .addOnSuccessListener(unused -> {
                    // ✅ מעלה unread למקבל בלבד (לא לתורם שלחץ Match)
                    // ה-receiver בצ'אט הוא המעוניין — הוא צריך לראות את ההודעה
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
                            });
                });
    }

    private void showDonationTakenBanner() {
        layoutBanner.setVisibility(View.VISIBLE);
        tvBannerMessage.setText("התרומה כבר נתפסה על ידי מישהו אחר.");
        btnDeleteChat.setVisibility(View.GONE);
        etMessage.setEnabled(false);
        etMessage.setHint("לא ניתן לשלוח הודעות");
        etMessage.setAlpha(0.5f);
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);
    }


    /**
     * ✅ באנר למשתמש לא פעיל – חוסם שליחה בלבד
     */
    private void showInactiveBanner() {
        layoutBanner.setVisibility(View.VISIBLE);
        tvBannerMessage.setText("משתמש זה אינו פעיל. לא ניתן לשלוח הודעות.");
        btnDeleteChat.setVisibility(View.GONE);

        etMessage.setEnabled(false);
        etMessage.setHint("לא ניתן לשלוח הודעות");
        etMessage.setAlpha(0.5f);
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);
    }

    /**
     * ✅ באנר למשתמש שנמחק – חוסם שליחה + מציג כפתור מחיקת שיחה
     */
    private void showDeletedUserBanner() {
        layoutBanner.setVisibility(View.VISIBLE);
        tvBannerMessage.setText("משתמש זה הוסר מהמערכת. ניתן למחוק את השיחה.");
        btnDeleteChat.setVisibility(View.VISIBLE);

        etMessage.setEnabled(false);
        etMessage.setHint("לא ניתן לשלוח הודעות");
        etMessage.setAlpha(0.5f);
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        // ✅ לחיצה על מחק שיחה
        btnDeleteChat.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("מחיקת שיחה")
                    .setMessage("האם למחוק את השיחה לצמיתות?")
                    .setPositiveButton("מחק", (d, w) -> {
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
                                        });
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null && chatId != null) {
            DatabaseService.getInstance().getChatService()
                    .removeListener(chatId, messagesListener);
        }
    }
}