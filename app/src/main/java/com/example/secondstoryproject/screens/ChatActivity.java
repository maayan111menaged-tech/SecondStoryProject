package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.models.Message;
import java.util.List;
import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.MessageAdapter;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends BaseActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private MessageAdapter messageAdapter;
    private ValueEventListener messagesListener;

    private String chatId;
    private String currentUserId;
    private boolean currentUserIsAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("CHAT_ID");
        String otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");

        User currentUser = SharedPreferencesUtil.getUser(this);
        android.util.Log.d("ChatActivity", "currentUser: " + currentUser);
        android.util.Log.d("ChatActivity", "chatId: " + chatId);

        if (currentUser == null || chatId == null) {
            android.util.Log.d("ChatActivity", "finish נקרא!");
            finish();
            return;
        }
        currentUserId = currentUser.getId();
        currentUserIsAdmin = currentUser.isAdmin();
        // כותרת
        TextView tvTitle = findViewById(R.id.tv_chat_title);
        tvTitle.setText(otherUserName != null ? "שיחה עם " + otherUserName : "שיחה");

        // RecyclerView
        rvMessages = findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // הודעות חדשות למטה
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(currentUserId);
        rvMessages.setAdapter(messageAdapter);

        // כפתור שליחה
        etMessage = findViewById(R.id.et_message);
        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());

        // האזנה להודעות
        listenToMessages();
    }

    private void listenToMessages() {
        String currentUserIdNew = currentUserId;
        if(currentUserIsAdmin){
            currentUserIdNew = "admin";
        }

        DatabaseService.getInstance().getChatService()
                .resetUnread(chatId, currentUserIdNew);

        messagesListener = DatabaseService.getInstance()
                .getChatService()
                .listenToMessages(chatId, new IDatabaseService.DatabaseCallback<List<Message>>() {
                    @Override
                    public void onCompleted(java.util.List<com.example.secondstoryproject.models.Message> messages) {
                        runOnUiThread(() -> {
                            messageAdapter.setMessages(messages);
                            // גלול למטה אוטומטית
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
                .sendMessage(chatId, currentUserId, text,
                        currentUserIsAdmin,
                        new IDatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                // הודעה נשלחה — ה-listener יעדכן אוטומטית
                            }
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
        // הסרת ה-listener כשיוצאים מהמסך
        if (messagesListener != null && chatId != null) {
            DatabaseService.getInstance().getChatService()
                    .removeListener(chatId, messagesListener);
        }
    }
}