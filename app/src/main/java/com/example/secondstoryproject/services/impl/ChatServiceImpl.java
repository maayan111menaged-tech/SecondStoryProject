package com.example.secondstoryproject.services.impl;

import com.example.secondstoryproject.models.Chat;
import com.example.secondstoryproject.models.Message;
import com.example.secondstoryproject.services.IChatService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatServiceImpl implements IChatService {

    private final DatabaseReference chatsRef;
    private final DatabaseReference usersRef;

    public ChatServiceImpl() {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app");
        this.chatsRef = db.getReference("chats");
        this.usersRef = db.getReference("users");
    }

    @Override
    public void getOrCreateDonationChat(String donationId, String giverId,
                                        String receiverId, IDatabaseService.DatabaseCallback<String> callback) {

        String chatId = "donation_" + donationId + "_" + receiverId;
        DatabaseReference metaRef = chatsRef.child(chatId).child("metadata");

        metaRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }
            if (!task.getResult().exists()) {
                Chat chat = new Chat(chatId, "donation", donationId, giverId, receiverId);
                metaRef.setValue(chat)
                        .addOnSuccessListener(unused -> {
                            usersRef.child(giverId).child("chats").child(chatId).setValue(true);
                            usersRef.child(receiverId).child("chats").child(chatId).setValue(true);
                            callback.onCompleted(chatId);
                        })
                        .addOnFailureListener(callback::onFailed);
            } else {
                callback.onCompleted(chatId);
            }
        });
    }

    @Override
    public void sendMessage(String chatId, String senderId, String text,
                            IDatabaseService.DatabaseCallback<Void> callback) {

        DatabaseReference messagesRef = chatsRef.child(chatId).child("messages");
        String messageId = messagesRef.push().getKey();
        Message message = new Message(messageId, senderId, text, System.currentTimeMillis());

        messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(unused -> {
                    DatabaseReference metaRef = chatsRef.child(chatId).child("metadata");
                    metaRef.child("lastMessage").setValue(text);
                    metaRef.child("lastTimestamp").setValue(System.currentTimeMillis());

                    // מגדילים unread למשתמש השני
                    metaRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot meta = task.getResult();
                            String giverId = meta.child("giverId").getValue(String.class);
                            String receiverId = meta.child("receiverId").getValue(String.class);

                            // הצד השני — לא השולח
                            String otherUserId = senderId.equals(giverId) ? receiverId : giverId;
                            if (otherUserId != null) {
                                incrementUnread(chatId, otherUserId);
                            }
                        }
                    });

                    callback.onCompleted(null);
                })
                .addOnFailureListener(callback::onFailed);
    }
    @Override
    public ValueEventListener listenToMessages(String chatId,
                                               IDatabaseService.DatabaseCallback<List<Message>> callback) {

        DatabaseReference messagesRef = chatsRef.child(chatId).child("messages");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Message msg = child.getValue(Message.class);
                    if (msg != null) {
                        msg.setId(child.getKey());
                        messages.add(msg);
                    }
                }
                callback.onCompleted(messages);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailed(error.toException());
            }
        };

        messagesRef.addValueEventListener(listener);
        return listener;
    }

    @Override
    public void getUserChats(String userId,
                             IDatabaseService.DatabaseCallback<List<Chat>> callback) {

        usersRef.child(userId).child("chats").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailed(task.getException());
                        return;
                    }

                    List<String> chatIds = new ArrayList<>();
                    for (DataSnapshot child : task.getResult().getChildren()) {
                        chatIds.add(child.getKey());
                    }

                    if (chatIds.isEmpty()) {
                        callback.onCompleted(new ArrayList<>());
                        return;
                    }

                    List<Chat> chats = new ArrayList<>();
                    final int[] count = {0};

                    for (String chatId : chatIds) {
                        chatsRef.child(chatId).child("metadata").get()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful() && t.getResult().exists()) {
                                        Chat chat = t.getResult().getValue(Chat.class);
                                        if (chat != null) {
                                            chat.setId(chatId);

                                            // טוענים את ה-unread לצאט הזה
                                            DataSnapshot meta = t.getResult();
                                            Object unreadObj = meta.child("unread_" + userId).getValue();
                                            int unread = unreadObj != null ?
                                                    ((Long) unreadObj).intValue() : 0;
                                            chat.setUnreadCount(unread);

                                            chats.add(chat);
                                        }
                                    }
                                    count[0]++;
                                    if (count[0] == chatIds.size()) {
                                        callback.onCompleted(chats);
                                    }
                                });
                    }
                });
    }
    @Override
    public void removeListener(String chatId, ValueEventListener listener) {
        chatsRef.child(chatId).child("messages").removeEventListener(listener);
    }

    @Override
    public void incrementUnread(String chatId, String userId) {
        DatabaseReference ref = chatsRef.child(chatId)
                .child("metadata")
                .child("unread_" + userId);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Integer current = task.getResult().getValue(Integer.class);
                ref.setValue(current != null ? current + 1 : 1);
            }
        });
    }

    @Override
    public void resetUnread(String chatId, String userId) {
        chatsRef.child(chatId)
                .child("metadata")
                .child("unread_" + userId)
                .setValue(0);
    }

    @Override
    public ValueEventListener listenToUnreadCount(String chatId, String userId,
                                                  IDatabaseService.DatabaseCallback<Integer> callback) {
        DatabaseReference ref = chatsRef.child(chatId)
                .child("metadata")
                .child("unread_" + userId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Integer count = snapshot.getValue(Integer.class);
                callback.onCompleted(count != null ? count : 0);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailed(error.toException());
            }
        };
        ref.addValueEventListener(listener);
        return listener;
    }
}