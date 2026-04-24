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

/**
 * Implementation of {@link IChatService} using Firebase Realtime Database.
 * <p>
 * Responsible for managing chats between users in the application.
 * Chats can be:
 * <ul>
 *     <li>Donation chats — between giver and receiver</li>
 *     <li>Admin chats — between user and system/admin team</li>
 * </ul>
 * </p>
 * <p>
 * This service handles:
 * <ul>
 *     <li>Creating chats (if not already existing)</li>
 *     <li>Sending messages</li>
 *     <li>Listening to real-time messages</li>
 *     <li>Managing unread message counters</li>
 *     <li>Fetching user chats with additional display data</li>
 * </ul>
 * </p>
 * <p>
 * All operations are asynchronous and return results via {@link IDatabaseService.DatabaseCallback}.
 * </p>
 */
public class ChatServiceImpl implements IChatService {

    /** Reference to chats node in Firebase */
    private final DatabaseReference chatsRef;

    /** Reference to users node in Firebase */
    private final DatabaseReference usersRef;

    /**
     * Constructor — initializes Firebase references.
     */
    public ChatServiceImpl() {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app");
        this.chatsRef = db.getReference("chats");
        this.usersRef = db.getReference("users");
    }

    /**
     * Creates a donation chat if it does not exist, otherwise returns existing chat ID.
     * @param donationId ID of the donation
     * @param giverId    ID of the giver (owner of donation)
     * @param receiverId ID of the interested user
     * @param callback   returns chat ID
     */
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

    /**
     * Sends a message in a chat and updates metadata (last message, timestamp, unread count).
     * @param chatId   chat ID
     * @param senderId sender user ID
     * @param text     message content
     * @param callback completion callback
     */
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

                            String otherUserId;
                            String type = meta.child("type").getValue(String.class);

                            if ("admin".equals(type)) {
                                // אם השולח הוא אדמין → unread למשתמש (receiverId)
                                // אם השולח הוא משתמש → unread ל"admin"
                                otherUserId = senderIsAdmin ? receiverId : "admin";
                            } else {
                                String giverId = meta.child("giverId").getValue(String.class);
                                otherUserId = senderId.equals(giverId) ? receiverId : giverId;
                            }

                            if (otherUserId != null) {
                                incrementUnread(chatId, otherUserId);
                            }
                        }
                    });

                    callback.onCompleted(null);
                })
                .addOnFailureListener(callback::onFailed);
    }
    /**
     * Listens in real-time to messages in a chat.
     * @param chatId   chat ID
     * @param callback returns list of messages
     * @return Firebase listener (must be removed manually)
     */
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

    /**
     * Retrieves all chats of a user and enriches them with display data (names, donation titles).
     * @param userId   user ID
     * @param callback returns list of chats
     */
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

                                            // unread
                                            Object unreadObj = t.getResult()
                                                    .child("unread_" + userId).getValue();
                                            int unread = unreadObj != null ?
                                                    ((Long) unreadObj).intValue() : 0;
                                            chat.setUnreadCount(unread);

                                            // מי הצד השני
                                            String otherUserId = userId.equals(chat.getGiverId())
                                                    ? chat.getReceiverId() : chat.getGiverId();
                                            chat.setOtherUserId(otherUserId);

                                            chats.add(chat);
                                        }
                                    }
                                    count[0]++;
                                    if (count[0] == chatIds.size()) {
                                        // עכשיו נשלוף שמות — משתמשים ותרומות
                                        enrichChatsWithNames(chats, userId, callback);
                                    }
                                });
                    }
                });
    }


    @Override
    public void getAllAdminChats(IDatabaseService.DatabaseCallback<List<Chat>> callback) {
        chatsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }

            List<String> adminChatIds = new ArrayList<>();
            for (DataSnapshot child : task.getResult().getChildren()) {
                if (child.getKey() != null && child.getKey().startsWith("admin_")) {
                    adminChatIds.add(child.getKey());
                }
            }

            if (adminChatIds.isEmpty()) {
                callback.onCompleted(new ArrayList<>());
                return;
            }

            List<Chat> chats = new ArrayList<>();
            final int[] count = {0};

            for (String chatId : adminChatIds) {
                chatsRef.child(chatId).child("metadata").get()
                        .addOnCompleteListener(t -> {
                            if (t.isSuccessful() && t.getResult().exists()) {
                                Chat chat = t.getResult().getValue(Chat.class);
                                if (chat != null) {
                                    chat.setId(chatId);

                                    // unread — סך כל ההודעות הלא נקראות מהמשתמש
                                    Object unreadObj = t.getResult()
                                            .child("unread_admin").getValue();
                                    int unread = unreadObj != null ?
                                            ((Long) unreadObj).intValue() : 0;
                                    chat.setUnreadCount(unread);

                                    chats.add(chat);
                                }
                            }
                            count[0]++;
                            if (count[0] == adminChatIds.size()) {
                                enrichAdminChatsWithNames(chats, callback);
                            }
                        });
            }
        });
    }

    // שולף את שם המשתמש לכל צאט אדמין
    private void enrichAdminChatsWithNames(List<Chat> chats,
                                           IDatabaseService.DatabaseCallback<List<Chat>> callback) {

        if (chats.isEmpty()) {
            callback.onCompleted(chats);
            return;
        }

        final int[] count = {0};
        final int total = chats.size();

        for (Chat chat : chats) {
            String userId = chat.getReceiverId(); // המשתמש הוא תמיד receiverId בצאט אדמין
            usersRef.child(userId).get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                            String userName = userTask.getResult()
                                    .child("userName").getValue(String.class);
                            chat.setOtherUserName(userName.trim());
                        }
                        count[0]++;
                        if (count[0] == total) callback.onCompleted(chats);
                    });
        }
    }


    /**
     * Enriches chat objects with additional display data:
     * user names and donation names.
     */
    private void enrichChatsWithNames(List<Chat> chats, String userId,
                                      IDatabaseService.DatabaseCallback<List<Chat>> callback) {

        if (chats.isEmpty()) {
            callback.onCompleted(chats);
            return;
        }

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

            // שולפים שם משתמש שני
            final Chat currentChat = chat;
            usersRef.child(chat.getOtherUserId()).get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                            String userName = userTask.getResult()
                                    .child("userName").getValue(String.class);
                            currentChat.setOtherUserName(userName.trim());
                        }

                        // שולפים שם תרומה
                        if (currentChat.getDonationId() != null) {
                            FirebaseDatabase.getInstance(
                                            "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                                    .getReference("donations")
                                    .child(currentChat.getDonationId()).get()
                                    .addOnCompleteListener(donTask -> {
                                        if (donTask.isSuccessful() && donTask.getResult().exists()) {
                                            String donName = donTask.getResult()
                                                    .child("name").getValue(String.class);
                                            currentChat.setDonationName(donName != null ? donName : "");
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


    /**
     * Removes a real-time listener from a chat.
     */
    @Override
    public void removeListener(String chatId, ValueEventListener listener) {
        chatsRef.child(chatId).child("messages").removeEventListener(listener);
    }

    /**
     * Increments unread message count for a user in a chat.
     */
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

    /**
     * Resets unread message count for a user.
     */
    @Override
    public void resetUnread(String chatId, String userId) {
        chatsRef.child(chatId)
                .child("metadata")
                .child("unread_" + userId)
                .setValue(0);
    }


    /**
     * Listens in real-time to unread message count for a specific user.
     */
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

    /**
     * Creates or retrieves a chat between a user and the admin team.
     */
    @Override
    public void getOrCreateAdminChat(String userId,
                                     IDatabaseService.DatabaseCallback<String> callback) {

        String chatId = "admin_" + userId;
        DatabaseReference metaRef = chatsRef.child(chatId).child("metadata");

        metaRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }
            if (!task.getResult().exists()) {
                Chat chat = new Chat(chatId, "admin", null, "admin", userId);
                metaRef.setValue(chat)
                        .addOnSuccessListener(unused -> {
                            // מוסיפים אינדקס רק למשתמש — לא לאדמינים ספציפיים
                            usersRef.child(userId).child("chats").child(chatId).setValue(true);
                            callback.onCompleted(chatId);
                        })
                        .addOnFailureListener(callback::onFailed);
            } else {
                callback.onCompleted(chatId);
            }
        });
    }

    @Override
    public void deleteAdminChat(String userId, IDatabaseService.DatabaseCallback<Void> callback) {
        String chatId = "admin_" + userId;

        // מוחקים את הצאט עצמו
        chatsRef.child(chatId).removeValue()
                .addOnSuccessListener(unused -> {
                    // מוחקים את האינדקס מה-users
                    usersRef.child(userId).child("chats").child(chatId).removeValue()
                            .addOnSuccessListener(unused2 -> callback.onCompleted(null))
                            .addOnFailureListener(callback::onFailed);
                })
                .addOnFailureListener(callback::onFailed);
    }

}