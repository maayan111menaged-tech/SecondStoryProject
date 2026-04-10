package com.example.secondstoryproject.services;

import com.example.secondstoryproject.models.Chat;
import com.example.secondstoryproject.models.Message;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public interface IChatService {

    void getOrCreateDonationChat(String donationId, String giverId,
                                 String receiverId, IDatabaseService.DatabaseCallback<String> callback);

    void sendMessage(String chatId, String senderId, String text,
                     IDatabaseService.DatabaseCallback<Void> callback);

    ValueEventListener listenToMessages(String chatId,
                                        IDatabaseService.DatabaseCallback<List<Message>> callback);

    void getUserChats(String userId,
                      IDatabaseService.DatabaseCallback<List<Chat>> callback);

    void removeListener(String chatId, ValueEventListener listener);


    void incrementUnread(String chatId, String userId);
    void resetUnread(String chatId, String userId);
    ValueEventListener listenToUnreadCount(String chatId, String userId,
                                           IDatabaseService.DatabaseCallback<Integer> callback);

    void getOrCreateAdminChat(String userId,
                              IDatabaseService.DatabaseCallback<String> callback);
}