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

/**
 * RecyclerView Adapter for displaying chat conversations list.
 * Each item includes:
 * - Chat name (user or admin)
 * - Last message or donation context
 * - Last message timestamp
 * - Unread messages count
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    /**
     * Listener for chat click events.
     */
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    /** List of chats displayed */
    private List<Chat> chats = new ArrayList<>();

    /** Click listener */
    private final OnChatClickListener listener;

    /** Time formatter for displaying last message time */
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    /**
     * Constructor.
     * @param listener click listener for chat items
     */
    public ChatListAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the chat list.
     * @param chats list of chats
     */
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

    /**
     * ViewHolder representing a single chat item.
     */
    class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvLastMessage, tvTime, tvUnread;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_chat_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_chat_time);
            tvUnread = itemView.findViewById(R.id.tv_unread_count);
        }

        /**
         * Binds chat data to the UI components.
         * @param chat chat object
         */
        void bind(Chat chat) {
            // Display chat name and context
            if ("admin".equals(chat.getType())) {
                tvName.setText("צוות Second Story");
                tvLastMessage.setText("פנייה לצוות");
            } else {
                String name = chat.getOtherUserName() != null ? chat.getOtherUserName() : "";
                String donation = chat.getDonationName() != null ? chat.getDonationName() : "";
                tvName.setText(name);
                tvLastMessage.setText(!donation.isEmpty() ? "📦 " + donation : "");
            }

            // Override with last message if exists
            String last = chat.getLastMessage();
            if (last != null && !last.isEmpty()) {
                tvLastMessage.setText(last);
            }

            // Display last message time
            if (chat.getLastTimestamp() > 0) {
                tvTime.setText(timeFormat.format(new Date(chat.getLastTimestamp())));
            }

            // Display unread messages count
            int unread = chat.getUnreadCount();
            if (unread > 0) {
                tvUnread.setVisibility(View.VISIBLE);
                tvUnread.setText(String.valueOf(unread));
            } else {
                tvUnread.setVisibility(View.GONE);
            }

            // Handle click event
            itemView.setOnClickListener(v -> listener.onChatClick(chat));
        }
    }
}