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
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
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

        listenToMessages();

        // ✅ בדיקת מצב הצד השני – נמחק או לא פעיל
        checkChatStatus(otherUserId);
    }

    /**
     * קורא את כל ה-metadata בקריאה אחת.
     * 1. donorDeleted=true  → באנר מחיקה + כפתור מחיקת צ'אט
     * 2. הצד השני לא פעיל  → באנר השבתה בלבד
     */
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

                    // שלב 1: בדיקת donorDeleted
                    Boolean isDeleted = meta.child("donorDeleted").getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isDeleted)) {
                        runOnUiThread(this::showDeletedUserBanner);
                        return;
                    }

                    // שלב 2: זיהוי הצד השני מה-metadata
                    String resolvedOtherUserId = otherUserIdFromIntent;

                    if (resolvedOtherUserId == null) {
                        String type       = meta.child("type").getValue(String.class);
                        String giverId    = meta.child("giverId").getValue(String.class);
                        String receiverId = meta.child("receiverId").getValue(String.class);

                        if ("admin".equals(type)) {
                            // אדמין – הצד השני הוא ה-receiver
                            // יוזר רגיל – הצד השני הוא "admin" (אין User object)
                            resolvedOtherUserId = currentUserIsAdmin ? receiverId : null;
                        } else {
                            resolvedOtherUserId = currentUserId.equals(giverId)
                                    ? receiverId : giverId;
                        }
                    }

                    // אם הצד השני הוא "admin" (מחרוזת) – אין User לבדוק
                    if (resolvedOtherUserId == null
                            || "admin".equals(resolvedOtherUserId)) return;

                    // שלב 3: בדיקת isActive של הצד השני
                    final String finalOtherUserId = resolvedOtherUserId;
                    DatabaseService.getInstance().getUserService()
                            .get(finalOtherUserId, new IDatabaseService.DatabaseCallback<User>() {
                                @Override
                                public void onCompleted(User otherUser) {
                                    if (otherUser != null && !otherUser.isActive()) {
                                        runOnUiThread(() -> showInactiveBanner());
                                    }
                                }
                                @Override
                                public void onFailed(Exception e) {
                                    android.util.Log.e("ChatActivity",
                                            "Failed to check user status", e);
                                }
                            });
                });
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