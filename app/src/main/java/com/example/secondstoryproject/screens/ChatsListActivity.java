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

// Chat list screen — displays all conversations for the current user or admin.
// Shows an empty state when no chats exist, sorted by most recent message.
public class ChatsListActivity extends BaseActivity {

    // highlights the chat icon in the bottom navigation bar
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

    // reloads chats every time the screen becomes visible to reflect new messages
    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }

    // fetches chats from the DB — all admin chats for admins, user-specific chats otherwise
    private void loadChats() {
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) return;

        IDatabaseService.DatabaseCallback<List<Chat>> callback =
                new IDatabaseService.DatabaseCallback<List<Chat>>() {
                    @Override
                    public void onCompleted(List<Chat> chats) {
                        // sorts chats so the most recently active conversation appears first
                        chats.sort((a, b) ->
                                Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));
                        runOnUiThread(() -> {
                            chatListAdapter.setChats(chats);
                            // toggles between the list and the empty state view
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
            databaseService.getChatService().getAllAdminChats(callback);
        } else {
            databaseService.getChatService()
                    .getUserChats(currentUser.getId(), callback);
        }
    }

    // opens the full chat screen and passes the chat ID, other user's name and ID
    private void openChat(Chat chat) {
        android.util.Log.d("ChatDebug", "chat.getOtherUserId()=" + chat.getOtherUserId());

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_ID", chat.getId());
        intent.putExtra("OTHER_USER_NAME", chat.getOtherUserName());
        intent.putExtra("OTHER_USER_ID", chat.getOtherUserId());
        startActivity(intent);
    }
}