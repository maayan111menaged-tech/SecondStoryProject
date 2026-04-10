package com.example.secondstoryproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    private List<Chat> chats = new ArrayList<>();
    private final OnChatClickListener listener;
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatListAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() { return chats.size(); }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvLastMessage, tvTime, tvUnread;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_chat_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_chat_time);
            tvUnread = itemView.findViewById(R.id.tv_unread_count);
        }

        void bind(Chat chat) {
            // שם + תרומה
            if ("admin".equals(chat.getType())) {
                tvName.setText("צוות Second Story");
                tvLastMessage.setText("פנייה לצוות");
            } else {
                String name = chat.getOtherUserName() != null ? chat.getOtherUserName() : "";
                String donation = chat.getDonationName() != null ? chat.getDonationName() : "";
                tvName.setText(name);
                tvLastMessage.setText(!donation.isEmpty() ? "📦 " + donation : "");
            }

            // הודעה אחרונה — רק אם יש
            String last = chat.getLastMessage();
            if (last != null && !last.isEmpty()) {
                tvLastMessage.setText(last);
            }

            // זמן
            if (chat.getLastTimestamp() > 0) {
                tvTime.setText(timeFormat.format(new Date(chat.getLastTimestamp())));
            }

            // עיגול
            int unread = chat.getUnreadCount();
            if (unread > 0) {
                tvUnread.setVisibility(View.VISIBLE);
                tvUnread.setText(String.valueOf(unread));
            } else {
                tvUnread.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onChatClick(chat));
        }
    }
}