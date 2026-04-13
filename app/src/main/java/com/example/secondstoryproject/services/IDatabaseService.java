package com.example.secondstoryproject.services;

/**
 * Service locator interface for accessing all database-related services.
 * This is the main entry point for database operations in the application.
 * Instead of exposing database logic directly, it provides access to
 * separate service interfaces for each domain:
 * - Users
 * - Donations
 * - Chats
 * <p>Usage example:</p>
 * <pre>{@code
 * IDatabaseService db = DatabaseService.getInstance();
 * db.getUserService().get(userId, callback);
 * db.getDonationService().getAll(callback);
 * db.getChatService().getChatsForUser(userId, callback);
 * }</pre>
 * @see IUserService
 * @see IDonationService
 * @see IChatService
 * @see DatabaseService
 */
public interface IDatabaseService {

    /**
     * Returns the user service for user-related operations.
     * @return implementation of {@link IUserService}
     */
    IUserService getUserService();

    /**
     * Returns the donation service for donation-related operations.
     * @return implementation of {@link IDonationService}
     */
    IDonationService getDonationService();

    /**
     * Returns the chat service for chat-related operations.
     * @return implementation of {@link IChatService}
     */
    IChatService getChatService();

    /**
     * Generic callback interface for asynchronous database operations.
     * All database operations are asynchronous.
     * Use this callback to handle success or failure.
     * <p>Exactly one method will be called:</p>
     * - {@link #onCompleted(Object)} on success
     * - {@link #onFailed(Exception)} on failure
     * @param <T> type of the result returned on success
     */
    interface DatabaseCallback<T> {
        /**
         * Called when the operation completes successfully.
         * @param object result of the operation (can be null)
         */
        void onCompleted(T object);

        /**
         * Called when the operation fails.
         * @param e exception describing the failure
         */
        void onFailed(Exception e);
    }
}
