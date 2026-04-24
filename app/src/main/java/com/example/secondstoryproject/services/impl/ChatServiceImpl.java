package com.example.secondstoryproject.services.impl;

import com.example.secondstoryproject.models.Chat;
import com.example.secondstoryproject.models.Message;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
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
            if (!task.isSuccessful()) { callback.onFailed(task.getException()); return; }
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
                            boolean senderIsAdmin,
                            IDatabaseService.DatabaseCallback<Void> callback) {
        DatabaseReference messagesRef = chatsRef.child(chatId).child("messages");
        String messageId = messagesRef.push().getKey();
        Message message = new Message(messageId, senderId, text, System.currentTimeMillis());
        message.setAdminSender(senderIsAdmin);

        messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(unused -> {
                    DatabaseReference metaRef = chatsRef.child(chatId).child("metadata");
                    metaRef.child("lastMessage").setValue(text);
                    metaRef.child("lastTimestamp").setValue(System.currentTimeMillis());

                    metaRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot meta = task.getResult();
                            String receiverId = meta.child("receiverId").getValue(String.class);
                            String type = meta.child("type").getValue(String.class);
                            String otherUserId;
                            if ("admin".equals(type)) {
                                otherUserId = senderIsAdmin ? receiverId : "admin";
                            } else {
                                String giverId = meta.child("giverId").getValue(String.class);
                                otherUserId = senderId.equals(giverId) ? receiverId : giverId;
                            }
                            if (otherUserId != null) incrementUnread(chatId, otherUserId);
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
                    if (msg != null) { msg.setId(child.getKey()); messages.add(msg); }
                }
                callback.onCompleted(messages);
            }
            @Override
            public void onCancelled(DatabaseError error) { callback.onFailed(error.toException()); }
        };
        messagesRef.addValueEventListener(listener);
        return listener;
    }

    @Override
    public void getUserChats(String userId, IDatabaseService.DatabaseCallback<List<Chat>> callback) {
        usersRef.child(userId).child("chats").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) { callback.onFailed(task.getException()); return; }
                    List<String> chatIds = new ArrayList<>();
                    for (DataSnapshot child : task.getResult().getChildren()) chatIds.add(child.getKey());
                    if (chatIds.isEmpty()) { callback.onCompleted(new ArrayList<>()); return; }

                    List<Chat> chats = new ArrayList<>();
                    final int[] count = {0};
                    for (String chatId : chatIds) {
                        chatsRef.child(chatId).child("metadata").get()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful() && t.getResult().exists()) {
                                        Chat chat = t.getResult().getValue(Chat.class);
                                        if (chat != null) {
                                            chat.setId(chatId);
                                            Object unreadObj = t.getResult().child("unread_" + userId).getValue();
                                            chat.setUnreadCount(unreadObj != null ? ((Long) unreadObj).intValue() : 0);
                                            String otherUserId = userId.equals(chat.getGiverId())
                                                    ? chat.getReceiverId() : chat.getGiverId();
                                            chat.setOtherUserId(otherUserId);
                                            chats.add(chat);
                                        }
                                    }
                                    count[0]++;
                                    if (count[0] == chatIds.size()) enrichChatsWithNames(chats, userId, callback);
                                });
                    }
                });
    }

    @Override
    public void getAllAdminChats(IDatabaseService.DatabaseCallback<List<Chat>> callback) {
        chatsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { callback.onFailed(task.getException()); return; }
            List<String> adminChatIds = new ArrayList<>();
            for (DataSnapshot child : task.getResult().getChildren()) {
                if (child.getKey() != null && child.getKey().startsWith("admin_"))
                    adminChatIds.add(child.getKey());
            }
            if (adminChatIds.isEmpty()) { callback.onCompleted(new ArrayList<>()); return; }

            List<Chat> chats = new ArrayList<>();
            final int[] count = {0};
            for (String chatId : adminChatIds) {
                chatsRef.child(chatId).child("metadata").get()
                        .addOnCompleteListener(t -> {
                            if (t.isSuccessful() && t.getResult().exists()) {
                                Chat chat = t.getResult().getValue(Chat.class);
                                if (chat != null) {
                                    chat.setId(chatId);
                                    Object unreadObj = t.getResult().child("unread_admin").getValue();
                                    chat.setUnreadCount(unreadObj != null ? ((Long) unreadObj).intValue() : 0);
                                    chats.add(chat);
                                }
                            }
                            count[0]++;
                            if (count[0] == adminChatIds.size()) enrichAdminChatsWithNames(chats, callback);
                        });
            }
        });
    }

    /**
     * ✅ מוחק את כל הצ'אטים של משתמש שנמחק.
     * מוחק את node הצ'אט מ-chats + האינדקס מ-users של שני הצדדים.
     */
    @Override
    public void deleteAllUserChats(String userId, IDatabaseService.DatabaseCallback<Void> callback) {
        // שלב 1: מביאים את כל הצ'אטים של המשתמש
        usersRef.child(userId).child("chats").get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailed(task.getException());
                        return;
                    }

                    List<String> chatIds = new ArrayList<>();

                    // צ'אט מנהלי
                    chatIds.add("admin_" + userId);

                    // צ'אטי תרומות
                    for (DataSnapshot child : task.getResult().getChildren()) {
                        String chatId = child.getKey();
                        if (chatId != null && !chatId.equals("admin_" + userId)) {
                            chatIds.add(chatId);
                        }
                    }

                    if (chatIds.isEmpty()) {
                        callback.onCompleted(null);
                        return;
                    }

                    final int[] done = {0};
                    final int total = chatIds.size();

                    for (String chatId : chatIds) {
                        // מוחקים את הצ'אט עצמו
                        chatsRef.child(chatId).removeValue()
                                .addOnCompleteListener(t -> {
                                    done[0]++;
                                    if (done[0] == total) {
                                        // מוחקים את האינדקס מה-user
                                        usersRef.child(userId).child("chats").removeValue()
                                                .addOnSuccessListener(u -> callback.onCompleted(null))
                                                .addOnFailureListener(callback::onFailed);
                                    }
                                });
                    }
                });
    }

    private void enrichAdminChatsWithNames(List<Chat> chats,
                                           IDatabaseService.DatabaseCallback<List<Chat>> callback) {
        if (chats.isEmpty()) { callback.onCompleted(chats); return; }
        final int[] count = {0};
        final int total = chats.size();
        for (Chat chat : chats) {
            String userId = chat.getReceiverId();
            usersRef.child(userId).get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                            String userName = userTask.getResult()
                                    .child("userName").getValue(String.class);
                            chat.setOtherUserName(
                                    userName != null ? userName.trim() : "משתמש שנמחק");
                        } else {
                            // node לא קיים = משתמש נמחק מה-DB
                            chat.setOtherUserName("משתמש שנמחק");
                        }
                        count[0]++;
                        if (count[0] == total) callback.onCompleted(chats);
                    });
        }
    }

    private void enrichChatsWithNames(List<Chat> chats, String userId,
                                      IDatabaseService.DatabaseCallback<List<Chat>> callback) {
        if (chats.isEmpty()) { callback.onCompleted(chats); return; }
        final int[] count = {0};
        final int total = chats.size();
        for (Chat chat : chats) {
            if ("admin".equals(chat.getType())) {
                chat.setOtherUserName("צוות Second Story");
                chat.setDonationName("");
                count[0]++;
                if (count[0] == total) callback.onCompleted(chats);
                continue;
            }
            final Chat currentChat = chat;
            usersRef.child(chat.getOtherUserId()).get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                            String userName = userTask.getResult()
                                    .child("userName").getValue(String.class);
                            currentChat.setOtherUserName(
                                    userName != null ? userName.trim() : "משתמש שנמחק");
                        } else {
                            currentChat.setOtherUserName("משתמש שנמחק");
                        }
                        if (currentChat.getDonationId() != null) {
                            FirebaseDatabase.getInstance(
                                            "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                                    .getReference("donations")
                                    .child(currentChat.getDonationId()).get()
                                    .addOnCompleteListener(donTask -> {
                                        if (donTask.isSuccessful() && donTask.getResult().exists()) {
                                            String donName = donTask.getResult()
                                                    .child("name").getValue(String.class);
                                            currentChat.setDonationName(
                                                    donName != null ? donName : "");
                                        }
                                        count[0]++;
                                        if (count[0] == total) callback.onCompleted(chats);
                                    });
                        } else {
                            count[0]++;
                            if (count[0] == total) callback.onCompleted(chats);
                        }
                    });
        }
    }

    @Override
    public void removeListener(String chatId, ValueEventListener listener) {
        chatsRef.child(chatId).child("messages").removeEventListener(listener);
    }

    @Override
    public void incrementUnread(String chatId, String userId) {
        DatabaseReference ref = chatsRef.child(chatId).child("metadata").child("unread_" + userId);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Integer current = task.getResult().getValue(Integer.class);
                ref.setValue(current != null ? current + 1 : 1);
            }
        });
    }

    @Override
    public void resetUnread(String chatId, String userId) {
        chatsRef.child(chatId).child("metadata").child("unread_" + userId).setValue(0);
    }

    @Override
    public ValueEventListener listenToUnreadCount(String chatId, String userId,
                                                  IDatabaseService.DatabaseCallback<Integer> callback) {
        DatabaseReference ref = chatsRef.child(chatId).child("metadata").child("unread_" + userId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Integer count = snapshot.getValue(Integer.class);
                callback.onCompleted(count != null ? count : 0);
            }
            @Override
            public void onCancelled(DatabaseError error) { callback.onFailed(error.toException()); }
        };
        ref.addValueEventListener(listener);
        return listener;
    }

    @Override
    public void getOrCreateAdminChat(String userId, IDatabaseService.DatabaseCallback<String> callback) {
        String chatId = "admin_" + userId;
        DatabaseReference metaRef = chatsRef.child(chatId).child("metadata");
        metaRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { callback.onFailed(task.getException()); return; }
            if (!task.getResult().exists()) {
                Chat chat = new Chat(chatId, "admin", null, "admin", userId);
                metaRef.setValue(chat)
                        .addOnSuccessListener(unused -> {
                            usersRef.child(userId).child("chats").child(chatId).setValue(true);
                            callback.onCompleted(chatId);
                        })
                        .addOnFailureListener(callback::onFailed);
            } else {
                callback.onCompleted(chatId);
            }
        });
    }

    /**
     * ✅ מסמן את כל הצ'אטים של משתמש שנמחק עם donorDeleted=true.
     * הצ'אטים לא נמחקים – הצד השני רואה באנר ויכול למחוק בעצמו.
     */
    @Override
    public void markUserAsDeleted(String userId, IDatabaseService.DatabaseCallback<Void> callback) {
        // סורק את כל הצ'אטים ב-DB במקום להסתמך על האינדקס של המשתמש
        // כך לא נפספס צ'אט גם אם האינדקס חסר
        chatsRef.get().addOnCompleteListener(allChatsTask -> {
            if (!allChatsTask.isSuccessful()) {
                callback.onFailed(allChatsTask.getException());
                return;
            }

            List<String> chatIds = new ArrayList<>();
            chatIds.add("admin_" + userId); // צ'אט מנהלי תמיד

            for (DataSnapshot chatSnap : allChatsTask.getResult().getChildren()) {
                String chatId = chatSnap.getKey();
                if (chatId == null) continue;

                DataSnapshot meta = chatSnap.child("metadata");
                String giverId    = meta.child("giverId").getValue(String.class);
                String receiverId = meta.child("receiverId").getValue(String.class);

                // המשתמש יכול להיות giver או receiver
                if (userId.equals(giverId) || userId.equals(receiverId)) {
                    if (!chatIds.contains(chatId)) chatIds.add(chatId);
                }
            }

            final int[] done = {0};
            final int total = chatIds.size();

            if (total == 0) {
                usersRef.child(userId).child("chats").removeValue()
                        .addOnCompleteListener(r -> callback.onCompleted(null));
                return;
            }

            for (String chatId : chatIds) {
                chatsRef.child(chatId).child("metadata")
                        .child("donorDeleted").setValue(true)
                        .addOnCompleteListener(t -> {
                            done[0]++;
                            if (done[0] == total) {
                                // מוחק את האינדקס של המשתמש המחוק
                                usersRef.child(userId).child("chats").removeValue()
                                        .addOnCompleteListener(r -> callback.onCompleted(null));
                            }
                        });
            }
        });
    }
    /**
     * ✅ מחיקה מלאה של צ'אט אחד מה-DB (נקרא ע"י הצד השני אחרי שרואה את הבאנר).
     */
    @Override
    public void deleteChat(String chatId, String userId, IDatabaseService.DatabaseCallback<Void> callback) {
        chatsRef.child(chatId).removeValue()
                .addOnSuccessListener(unused ->
                        usersRef.child(userId).child("chats").child(chatId).removeValue()
                                .addOnSuccessListener(u -> callback.onCompleted(null))
                                .addOnFailureListener(callback::onFailed))
                .addOnFailureListener(callback::onFailed);
    }

    @Override
    public void deleteAdminChat(String userId, IDatabaseService.DatabaseCallback<Void> callback) {
        String chatId = "admin_" + userId;
        chatsRef.child(chatId).removeValue()
                .addOnSuccessListener(unused ->
                        usersRef.child(userId).child("chats").child(chatId).removeValue()
                                .addOnSuccessListener(unused2 -> callback.onCompleted(null))
                                .addOnFailureListener(callback::onFailed))
                .addOnFailureListener(callback::onFailed);
    }
}