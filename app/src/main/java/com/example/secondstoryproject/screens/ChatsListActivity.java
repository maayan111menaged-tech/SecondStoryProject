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
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);

        rvChats = findViewById(R.id.rv_chats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        layoutEmpty = findViewById(R.id.layout_empty);

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
                                Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));
                        runOnUiThread(() -> {
                            chatListAdapter.setChats(chats);
                            if (chats.isEmpty()) {
                                rvChats.setVisibility(View.GONE);
                                layoutEmpty.setVisibility(View.VISIBLE);
                            } else {
                                rvChats.setVisibility(View.VISIBLE);
                                layoutEmpty.setVisibility(View.GONE);
                            }
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
            DatabaseService.getInstance().getChatService().getAllAdminChats(callback);
        } else {
            DatabaseService.getInstance().getChatService()
                    .getUserChats(currentUser.getId(), callback);
        }
    }

    private void openChat(Chat chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_ID", chat.getId());
        intent.putExtra("OTHER_USER_NAME", chat.getOtherUserName());
        intent.putExtra("OTHER_USER_ID", chat.getOtherUserId());
        startActivity(intent);
    }
}