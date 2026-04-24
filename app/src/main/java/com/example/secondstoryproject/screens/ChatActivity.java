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
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatActivity extends BaseActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
    private MessageAdapter messageAdapter;
    private ValueEventListener messagesListener;

    private String chatId;
    private String currentUserId;
    private boolean currentUserIsAdmin;

    private LinearLayout layoutInactiveUser;

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

        messageAdapter = new MessageAdapter(currentUserId, currentUserIsAdmin);
        rvMessages.setAdapter(messageAdapter);

        layoutInactiveUser = findViewById(R.id.layout_inactive_user_banner);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendMessage());

        listenToMessages();

        // ✅ בדיקת סטטוס הצד השני
        checkOtherUserActive(otherUserId);
    }

    /**
     * ✅ מחלץ את userId של הצד השני ובודק אם הוא פעיל.
     *
     * סדר עדיפויות:
     * 1. OTHER_USER_ID מה-Intent (הכי מדויק)
     * 2. חילוץ מה-chatId לפי הפורמט
     */
    private void checkOtherUserActive(String otherUserIdFromIntent) {
        String otherUserId = otherUserIdFromIntent;

        if (otherUserId == null) {
            if (chatId.startsWith("admin_")) {
                // "admin_receiverId"
                otherUserId = chatId.substring("admin_".length());

            } else if (chatId.startsWith("donation_")) {
                // "donation_donationId_receiverId" → החלק האחרון
                String[] parts = chatId.split("_");
                if (parts.length >= 3) {
                    String receiverId = parts[parts.length - 1];
                    // אם ה-receiver הוא המשתמש הנוכחי, הצד השני הוא ה-giver – לא ידוע כאן
                    otherUserId = receiverId.equals(currentUserId) ? null : receiverId;
                }
            }
        }

        // אדמין לא מושבת – לא צריך לבדוק
        if (otherUserId == null || otherUserId.equals("admin")) return;

        final String finalOtherUserId = otherUserId;
        DatabaseService.getInstance().getUserService().get(finalOtherUserId,
                new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User otherUser) {
                        if (otherUser != null && !otherUser.isActive()) {
                            runOnUiThread(() -> showInactiveBanner());
                        }
                    }
                    @Override
                    public void onFailed(Exception e) {
                        android.util.Log.e("ChatActivity", "Failed to check user status", e);
                    }
                });
    }

    /**
     * ✅ מציג באנר + חוסם קלט (disabled) במקום להסתיר.
     */
    private void showInactiveBanner() {
        // הצג באנר
        if (layoutInactiveUser != null) {
            layoutInactiveUser.setVisibility(View.VISIBLE);
        }

        // חסום כתיבה בשדה הטקסט
        etMessage.setEnabled(false);
        etMessage.setHint("לא ניתן לשלוח הודעות");
        etMessage.setAlpha(0.5f);

        // חסום כפתור שליחה
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);
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