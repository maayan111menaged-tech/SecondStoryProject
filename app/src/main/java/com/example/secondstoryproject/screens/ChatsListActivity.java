package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.ChatListAdapter;
import com.example.secondstoryproject.models.Chat;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

import java.util.List;

public class ChatsListActivity extends BaseActivity {

    @Override
    protected int getSelectedBottomNavItem() {
        return R.id.menu_chat;
    }

    private ChatListAdapter chatListAdapter;
    private RecyclerView rvChats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);

        rvChats = findViewById(R.id.rv_chats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        chatListAdapter = new ChatListAdapter(chat -> openChat(chat));
        rvChats.setAdapter(chatListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }

    private void loadChats() {
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) return;

        IDatabaseService.DatabaseCallback<List<Chat>> callback =
                new IDatabaseService.DatabaseCallback<List<Chat>>() {
                    @Override
                    public void onCompleted(List<Chat> chats) {
                        chats.sort((a, b) ->
                                Long.compare(b.getLastTimestamp(), a.getLastTimestamp())); // מיון לפי זמן ההודעה האחרונה
                        runOnUiThread(() -> {
                            chatListAdapter.setChats(chats);
                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(ChatsListActivity.this,
                                        "שגיאה בטעינת שיחות", Toast.LENGTH_SHORT).show());
                    }
                };

        if (currentUser.isAdmin()) {
            // אדמין — מביא את כל צ'אטי האדמין
            DatabaseService.getInstance().getChatService()
                    .getAllAdminChats(callback);
        } else {
            // משתמש רגיל
            DatabaseService.getInstance().getChatService()
                    .getUserChats(currentUser.getId(), callback);
        }
    }

    private void openChat(Chat chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_ID", chat.getId());
        intent.putExtra("OTHER_USER_NAME", chat.getOtherUserName());
        startActivity(intent);
    }
}