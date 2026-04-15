package com.example.secondstoryproject.services;

import com.example.secondstoryproject.models.Chat;
import com.example.secondstoryproject.models.Message;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

/// Service interface for chat-related operations.
/// <p>
/// Handles communication between users regarding donations,
/// including creating chats, sending messages, and tracking unread messages.
/// </p>
/// <p>
/// All operations are asynchronous and use {@link IDatabaseService.DatabaseCallback}.
/// </p>
/// @see Chat
/// @see Message
public interface IChatService {

    /// Get an existing chat for a donation or create a new one if it doesn't exist
    /// @param donationId The ID of the related donation
    /// @param giverId The ID of the user who created the donation
    /// @param receiverId The ID of the user interested in the donation
    /// @param callback Returns the chat ID
    void getOrCreateDonationChat(String donationId, String giverId,
                                 String receiverId, IDatabaseService.DatabaseCallback<String> callback);

    /// Send a message in a chat
    /// @param chatId The ID of the chat
    /// @param senderId The ID of the user sending the message
    /// @param text The message content
    /// @param callback Called when the message is successfully sent or failed
    void sendMessage(String chatId, String senderId, String text,
                     boolean senderIsAdmin,
                     IDatabaseService.DatabaseCallback<Void> callback);

    /// Listen for real-time updates of messages in a chat
    /// @param chatId The ID of the chat
    /// @param callback Returns the updated list of messages
    /// @return A {@link ValueEventListener} that can be removed later
    ValueEventListener listenToMessages(String chatId,
                                        IDatabaseService.DatabaseCallback<List<Message>> callback);

    /// Retrieve all chats for a specific user
    /// @param userId The ID of the user
    /// @param callback Returns a list of chats the user is part of
    void getUserChats(String userId,
                      IDatabaseService.DatabaseCallback<List<Chat>> callback);

    /// Remove a previously registered Firebase listener
    /// @param chatId The ID of the chat
    /// @param listener The listener to remove
    void removeListener(String chatId, ValueEventListener listener);

    /// Increase the unread message count for a specific user in a chat
    /// @param chatId The ID of the chat
    /// @param userId The ID of the user whose unread count should increase
    void incrementUnread(String chatId, String userId);

    /// Reset the unread message count for a specific user in a chat
    /// @param chatId The ID of the chat
    /// @param userId The ID of the user whose unread count should be reset
    void resetUnread(String chatId, String userId);
    /// Listen for real-time updates of unread message count
    /// @param chatId The ID of the chat
    /// @param userId The ID of the user
    /// @param callback Returns the unread message count
    /// @return A {@link ValueEventListener} that can be removed later
    ValueEventListener listenToUnreadCount(String chatId, String userId,
                                           IDatabaseService.DatabaseCallback<Integer> callback);

    /// Get or create a chat between a user and the admin team
    /// @param userId The ID of the user
    /// @param callback Returns the chat ID
    void getOrCreateAdminChat(String userId,
                              IDatabaseService.DatabaseCallback<String> callback);


    void getAllAdminChats(IDatabaseService.DatabaseCallback<List<Chat>> callback);
    void deleteAdminChat(String userId, IDatabaseService.DatabaseCallback<Void> callback);

}